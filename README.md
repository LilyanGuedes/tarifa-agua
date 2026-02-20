# Tarifa de Agua API

API REST para gerenciamento de tabelas tarifarias de agua e calculo de consumo por categoria de consumidor.

## Indice

- [Pre-requisitos](#pre-requisitos)
- [Configuracao do banco de dados](#configuracao-do-banco-de-dados)
- [Como executar](#como-executar)
- [Arquitetura](#arquitetura)
- [Modelagem de dados](#modelagem-de-dados)
- [Logica de calculo](#logica-de-calculo)
- [Endpoints da API](#endpoints-da-api)
- [Testes](#testes)
- [Tecnologias](#tecnologias)

---

## Pre-requisitos

| Ferramenta | Versao  |
|------------|---------|
| Java       | 17+     |
| PostgreSQL | 15+     |
| Maven      | 3.9+    |

> O projeto inclui o Maven Wrapper (`./mvnw`), entao nao e necessario ter o Maven instalado.

## Configuracao do banco de dados

1. Crie o banco no PostgreSQL:

```sql
CREATE DATABASE tarifa_agua;
```

2. Crie o arquivo `src/main/resources/application-local.yml` com as credenciais do seu banco:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tarifa_agua
    username: seu_usuario
    password: sua_senha
```

> Este arquivo esta no `.gitignore` e nao sera versionado.

As tabelas sao criadas automaticamente pelo **Flyway** na primeira execucao.

## Como executar

```bash
# clonar o repositorio
git clone https://github.com/LilyanGuedes/tarifa-agua.git
cd tarifa-agua

# executar a aplicacao (porta 8080)
./mvnw spring-boot:run
```

A API estara disponivel em `http://localhost:8080`.

---

## Arquitetura

O projeto segue uma arquitetura em camadas, separando responsabilidades de forma clara:

```
Controller          -> Recebe as requisicoes HTTP e delega para os services
  |
Service             -> Contem a logica de negocio (calculo, validacao, CRUD)
  |
Domain (Model)      -> Entidades JPA com regras de dominio encapsuladas
  |
Repository          -> Acesso ao banco de dados via Spring Data JPA
```

**Decisoes de design:**

- **Rich Domain Model**: as entidades possuem comportamento proprio (ex: `TariffTable.addCategory()`, `TariffTable.markAsDeleted()`). A logica de dominio fica nas entidades, nao nos services.
- **Validacao dedicada**: o `TariffTableValidator` centraliza todas as regras de validacao da tabela tarifaria (continuidade das faixas, categorias obrigatorias, vigencia).
- **Soft delete**: tabelas tarifarias nao sao removidas fisicamente do banco. Sao marcadas com status `DELETED` e `deletedAt`, preservando o historico.
- **Migrations com Flyway**: o schema do banco e versionado e evolui de forma controlada.

## Modelagem de dados

O sistema possui 3 entidades principais com o seguinte relacionamento:

```
TariffTable (Tabela Tarifaria)
  |
  | 1:N
  v
TariffCategory (Categoria de Consumidor)
  |
  | 1:N
  v
ConsumptionRange (Faixa de Consumo)
```

### TariffTable

Representa uma tabela tarifaria com periodo de vigencia e status de ciclo de vida.

| Campo      | Tipo         | Descricao                              |
|------------|--------------|----------------------------------------|
| id         | Long         | Identificador unico                    |
| name       | String (120) | Nome da tabela                         |
| validFrom  | LocalDate    | Inicio da vigencia                     |
| validTo    | LocalDate    | Fim da vigencia                        |
| status     | Enum         | ACTIVE, INACTIVE ou DELETED            |
| deletedAt  | DateTime     | Data do soft delete (se aplicavel)     |

### TariffCategory

Vincula uma tabela tarifaria a uma categoria de consumidor. Cada tabela deve conter as 4 categorias.

**Categorias:** `COMERCIAL`, `INDUSTRIAL`, `PARTICULAR`, `PUBLICO`

### ConsumptionRange

Define uma faixa de consumo em m3 com seu valor unitario.

| Campo      | Tipo          | Descricao                             |
|------------|---------------|---------------------------------------|
| rangeStart | Integer       | Inicio da faixa (m3)                  |
| rangeEnd   | Integer       | Fim da faixa (m3)                     |
| unitPrice  | BigDecimal    | Valor por m3 nesta faixa (R$)         |

**Regras das faixas:**
- Devem comecar em 0 m3
- Devem ser continuas (sem buracos entre faixas)
- Nao podem se sobrepor (garantido por constraint `EXCLUDE` no banco)
- O valor unitario deve ser >= 0

## Logica de calculo

O calculo da conta de agua funciona por **faixas progressivas**, similar ao imposto de renda. O consumo e distribuido entre as faixas da menor para a maior, e cada m3 e cobrado pelo valor da faixa correspondente.

### Como o codigo funciona

A classe `TariffCalculationService` e responsavel por todo o calculo. O fluxo funciona assim:

**1. Buscar a tabela vigente**

O metodo `findCurrentTable()` consulta o repositorio passando `LocalDate.now()` e retorna a primeira tabela cuja vigencia (`validFrom` / `validTo`) cobre a data atual e ignorando as que possuem deletedAt. Se nenhuma tabela for encontrada, retorna erro 422.

**2. Localizar a categoria**

Usando o metodo `findCategory()` da propria entidade `TariffTable`, busca a categoria informada na requisicao (ex: `INDUSTRIAL`). Isso evita uma query extra no banco — a busca e feita em memoria na lista de categorias ja carregada.


**3. Iterar pelas faixas com acumulador**

O nucleo do calculo usa uma variavel `restante` que comeca com o consumo total e vai sendo decrementada a cada faixa:

```
restante = consumoTotal (ex: 18)

Para cada faixa (ordenada por inicio):
  |
  |-- Se restante <= 0, para
  |
  |-- Calcula quantos m3 cabem nessa faixa:
  |     maxCobravel = fim - inicioEfetivo + 1
  |     m3Cobrados  = min(restante, maxCobravel)
  |
  |-- Calcula o subtotal:
  |     subtotal = m3Cobrados x valorUnitario
  |
  |-- Acumula no total e desconta do restante:
        total    += subtotal
        restante -= m3Cobrados
```

A faixa `0..10` é tratada de forma especial: como o inicio é 0, mas a cobranca comeca a partir do 1o m3, o codigo ajusta o `inicioCobrancaDaFaixa` para 1 quando `start == 0`. Isso garante que a faixa 0-10 cobre exatamente 10 unidades.

**5. Montar a resposta com detalhamento**

A cada faixa processada, um `RangeBreakdown` e adicionado ao detalhamento, contendo a faixa, os m3 cobrados, o valor unitario e o subtotal. O consumidor recebe a conta total e a discriminacao completa de como o valor foi calculado.

### Exemplo pratico

Categoria **INDUSTRIAL**, faixas `0-10 @ R$1,00` e `11-20 @ R$2,00`, consumo de **18 m3**:

```
restante = 18

Faixa 0-10:  maxCobravel = 10, m3Cobrados = 10
             subtotal = 10 x R$ 1,00 = R$ 10,00
             restante = 18 - 10 = 8

Faixa 11-20: maxCobravel = 10, m3Cobrados = 8
             subtotal = 8 x R$ 2,00  = R$ 16,00
             restante = 8 - 8 = 0

Total = R$ 10,00 + R$ 16,00 = R$ 26,00
```

---

## Endpoints da API

### POST /api/tabelas-tarifarias

Cria uma nova tabela tarifaria. Deve conter as 4 categorias obrigatorias.

**Request:**

```json
{
  "name": "Tabela 2025",
  "validFrom": "2025-01-01",
  "validTo": "2025-12-31",
  "categories": [
    {
      "category": "PARTICULAR",
      "ranges": [
        { "start": 0,  "end": 10,    "unitPrice": 2.50 },
        { "start": 11, "end": 20,    "unitPrice": 4.00 },
        { "start": 21, "end": 99999, "unitPrice": 6.00 }
      ]
    },
    {
      "category": "COMERCIAL",
      "ranges": [
        { "start": 0,  "end": 10,    "unitPrice": 3.00 },
        { "start": 11, "end": 20,    "unitPrice": 5.00 },
        { "start": 21, "end": 99999, "unitPrice": 7.50 }
      ]
    },
    {
      "category": "INDUSTRIAL",
      "ranges": [
        { "start": 0,  "end": 10,    "unitPrice": 4.00 },
        { "start": 11, "end": 20,    "unitPrice": 6.50 },
        { "start": 21, "end": 99999, "unitPrice": 9.00 }
      ]
    },
    {
      "category": "PUBLICO",
      "ranges": [
        { "start": 0,  "end": 10,    "unitPrice": 1.50 },
        { "start": 11, "end": 20,    "unitPrice": 2.50 },
        { "start": 21, "end": 99999, "unitPrice": 4.00 }
      ]
    }
  ]
}
```

**Response — 201 Created:**

```json
{
  "id": 1,
  "name": "Tabela 2025"
}
```

### GET /api/tabelas-tarifarias

Lista todas as tabelas tarifarias ativas com suas categorias e faixas.

**Response — 200 OK:**

```json
[
  {
    "id": 1,
    "name": "Tabela 2025",
    "validFrom": "2025-01-01",
    "validTo": "2025-12-31",
    "categories": [
      {
        "category": "PARTICULAR",
        "ranges": [
          { "start": 0,  "end": 10,    "unitPrice": 2.50 },
          { "start": 11, "end": 20,    "unitPrice": 4.00 },
          { "start": 21, "end": 99999, "unitPrice": 6.00 }
        ]
      }
    ]
  }
]
```

### DELETE /api/tabelas-tarifarias/{id}

Remove uma tabela tarifaria (soft delete).

**Response — 204 No Content**

---

### POST /api/calculos

Calcula o valor da conta de agua com base na categoria e consumo informados.

**Request:**

```json
{
  "categoria": "INDUSTRIAL",
  "consumo": 18
}
```

**Response — 200 OK:**

```json
{
  "categoria": "INDUSTRIAL",
  "consumoTotal": 18,
  "valorTotal": 26.00,
  "detalhamento": [
    {
      "faixa": { "inicio": 0, "fim": 10 },
      "m3Cobrados": 10,
      "valorUnitario": 1.00,
      "subtotal": 10.00
    },
    {
      "faixa": { "inicio": 11, "fim": 20 },
      "m3Cobrados": 8,
      "valorUnitario": 2.00,
      "subtotal": 16.00
    }
  ]
}
```

---

## Testes

O projeto conta com testes unitarios que cobrem as regras de negocio principais:

### TariffTableTest — Modelo de dominio
- Adicionar e buscar categorias na tabela
- Adicionar faixas de consumo nas categorias
- Soft delete (marcar como deletada)
- Protecao contra nome nulo
- Imutabilidade da lista de categorias

### TariffTableValidatorTest — Validacao da tabela tarifaria
- Rejeitar vigencia com data inicial maior que a final
- Rejeitar faixas vazias
- Rejeitar faixas que nao comecam em 0 m3
- Rejeitar faixas com buracos (nao continuas)
- Rejeitar intervalos invalidos
- Rejeitar preco negativo

### TariffCalculationServiceTest — Calculo de tarifa
- Calculo com consumo distribuido em multiplas faixas
- Calculo com consumo em uma unica faixa
- Calculo com consumo exato no limite da faixa
- Calculo com tres faixas
- Erro quando nao existe tabela vigente
- Erro quando a categoria nao esta na tabela
- Erro quando o consumo excede a cobertura das faixas

Para executar os testes:

```bash
./mvnw test
```

## Tecnologias

- **Spring Boot 4.0** — Framework web
- **Spring Data JPA** — Persistencia e acesso a dados
- **Flyway** — Versionamento do schema do banco
- **PostgreSQL** — Banco de dados relacional
- **Bean Validation** — Validacao de entrada
- **JUnit 5 + Mockito** — Testes unitarios
