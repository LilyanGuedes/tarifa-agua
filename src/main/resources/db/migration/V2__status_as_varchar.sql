alter table tariff_table
    alter column status drop default;

do $$
declare
c record;
begin
for c in
select conname
from pg_constraint
where conrelid = 'tariff_table'::regclass
          and contype = 'c'
          and pg_get_constraintdef(oid) ilike '%status%'
    loop
        execute format('alter table tariff_table drop constraint %I', c.conname);
end loop;
end $$;


alter table tariff_table
alter column status type varchar(20)
    using status::text;


alter table tariff_table
    alter column status set default 'ACTIVE';

do $$
begin
    if exists (select 1 from pg_type where typname = 'tariff_status') then
drop type tariff_status;
end if;
end $$;
