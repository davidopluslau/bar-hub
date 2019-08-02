db-create:
	docker run --name bar-hub-db -e POSTGRES_PASSWORD=postgres -e POSTGRES_USER=postgres -e POSTGRES_DB=postgres -p 54320:5432 -d postgres:11-alpine
	sleep 2
	PGPASSWORD='postgres' psql -h 127.0.0.1 -p 54320 -U postgres -c "CREATE ROLE bar_hub PASSWORD 'bar_hub' SUPERUSER CREATEDB CREATEROLE LOGIN INHERIT IN ROLE pg_monitor"
	PGPASSWORD='bar_hub' createdb --encoding=UTF8 --locale=en_US.utf8 --owner=bar_hub -h 127.0.0.1 -p 54320 -U bar_hub bar_hub 'The Hub Database'
	PGPASSWORD='bar_hub' psql -h 127.0.0.1 -p 54320 -U bar_hub -d bar_hub -c "CREATE SCHEMA bar_hub"

db-destroy:
	docker rm -f bar-hub-db > /dev/null 2>&1

db-migrate:
	PGPASSWORD='bar_hub' psql -h 127.0.0.1 -p 54320 -U bar_hub -d bar_hub -f 001__init.sql
