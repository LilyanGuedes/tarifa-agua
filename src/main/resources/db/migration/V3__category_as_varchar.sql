alter table tariff_category
    alter column category drop default;

do $$
declare
c record;
begin
for c in
select conname
from pg_constraint
where conrelid = 'tariff_category'::regclass
          and contype = 'c'
          and pg_get_constraintdef(oid) ilike '%category%'
    loop
        execute format('alter table tariff_category drop constraint', c.conname);
end loop;
end $$;

alter table tariff_category
alter column category type varchar(50)
    using category::text;

do $$
begin
    if exists (select 1 from pg_type where typname = 'consumer_category') then
drop type consumer_category;
end if;
end $$;
