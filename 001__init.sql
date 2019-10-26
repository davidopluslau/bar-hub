CREATE TABLE menu
(
    "version"    text NOT NULL,
    "n_drinks"   int  NOT NULL DEFAULT 0,
    "start_date" date NOT NULL,
    "end_date"   date          DEFAULT NULL,
    PRIMARY KEY ("version")
) WITHOUT OIDS;
CREATE INDEX "menus__start_date__idx" ON menu USING btree ("start_date" DESC);

CREATE TABLE drink
(
    "name"    text          NOT NULL,
    "version" text          NOT NULL,
    "cost"    numeric(6, 2) NOT NULL,
    "alcohol" numeric(6, 2) NOT NULL,
    "blurb"   text          NOT NULL,
    "is_pour" boolean       NOT NULL,
    PRIMARY KEY ("name", "version")
) WITHOUT OIDS;
CREATE INDEX "drinks__version__idx" ON drink USING btree ("version");
ALTER TABLE drink
    ADD CONSTRAINT "drinks__version__fk" FOREIGN KEY ("version") REFERENCES menu ("version") ON DELETE CASCADE ON UPDATE CASCADE;

CREATE TABLE menu_drink
(
    "menu_version"  text NOT NULL,
    "drink_name"    text NOT NULL,
    "drink_version" text NOT NULL,
    "position"      int  NOT NULL,
    PRIMARY KEY ("drink_name", "drink_version", "menu_version")
) WITHOUT OIDS;
CREATE INDEX "menu_drinks__menu_version__idx" ON menu_drink USING btree ("menu_version" DESC);
ALTER TABLE menu_drink
    ADD CONSTRAINT "menu_drinks__menu_version__fk" FOREIGN KEY ("menu_version") REFERENCES menu ("version") ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT "menu_drinks__drink_name__drinks_version_fk" FOREIGN KEY ("drink_name", "drink_version") REFERENCES drink ("name", "version") ON DELETE CASCADE ON UPDATE CASCADE;

CREATE TABLE friend
(
    "name"             text PRIMARY KEY,
    "first_drink_date" date DEFAULT NULL
) WITHOUT OIDS;

CREATE TABLE "order"
(
    "friend_name"  text          NOT NULL,
    "menu_version" text          NOT NULL,
    "raw_cost"     numeric(6, 2) NOT NULL,
    "discount"     numeric(6, 2) NOT NULL,
    "final_cost"   numeric(6, 2) NOT NULL,
    order_date     date          NOT NULL,
    "notes"        text DEFAULT NULL
) WITHOUT OIDS;
CREATE UNIQUE INDEX "orders__friend_name_date__idx" ON "order" USING btree ("friend_name", order_date);
CREATE INDEX "orders__menu_version__idx" ON "order" USING btree ("menu_version");
ALTER TABLE "order"
    ADD CONSTRAINT "orders__menu_version__fk" FOREIGN KEY ("menu_version") REFERENCES menu ("version") ON DELETE CASCADE ON UPDATE CASCADE;

CREATE TYPE payment_type AS enum ('cash', 'venmo');

CREATE TABLE payment
(
    "order_date"   date         NOT NULL,
    "friend_name"  text         NOT NULL,
    "amount"       numeric      NOT NULL,
    "payment_type" payment_type NOT NULL,
    payment_date         date         NOT NULL
) WITHOUT OIDS;
CREATE INDEX "payments__friend_name_order_date__idx" ON payment USING btree (friend_name, "order_date");
ALTER TABLE payment
    ADD CONSTRAINT "payments__friend_name__fk" FOREIGN KEY (friend_name) REFERENCES friend ("name") ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT "payments__order_friend__date__fk" FOREIGN KEY (friend_name, "order_date") REFERENCES "order" ("friend_name", order_date) ON DELETE CASCADE ON UPDATE CASCADE;

CREATE TABLE order_drink
(
    "order_date"    date          NOT NULL,
    "friend_name"   text          NOT NULL,
    "drink_name"    text          NOT NULL,
    "drink_version" text          NOT NULL,
    "quantity"      numeric(4, 2) NOT NULL,
    "menu_version"  text          NOT NULL,
    "cost"          numeric(6, 2) NOT NULL,
    "discount"      numeric(6, 2) NOT NULL,
    "is_first"      boolean       NOT NULL,
    PRIMARY KEY ("drink_name", "drink_version", "order_date", "friend_name")
) WITHOUT OIDS;
CREATE INDEX "order_drinks__friend_name_order_date__idx" ON order_drink USING btree ("friend_name", "order_date");
CREATE INDEX "order_drinks__menu_version__idx" ON order_drink USING btree ("menu_version");
ALTER TABLE order_drink
    ADD CONSTRAINT "order_drinks__friend_name_order_date__fk" FOREIGN KEY ("friend_name", "order_date") REFERENCES "order" ("friend_name", order_date) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT "order_drinks__menu_version_fk" FOREIGN KEY ("menu_version") REFERENCES menu ("version") ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT "order_drinks__drink_name__drinks_version_fk" FOREIGN KEY ("drink_name", "drink_version") REFERENCES drink ("name", "version") ON DELETE CASCADE ON UPDATE CASCADE;

CREATE TYPE unit_type AS enum ('mL', 'g', 'oz', 'dash', 'barspoon');

CREATE TABLE component
(
    "name"                 text PRIMARY KEY,
    "single_cost"          numeric(6, 2) NOT NULL,
    "adjusted_single_cost" numeric(6, 2) NOT NULL,
    "single_size"          int           NOT NULL,
    "unit_type"            unit_type     NOT NULL,
    "unit_cost"            numeric(7, 4) NOT NULL,
    "alcohol_percentage"   numeric(4, 2) NOT NULL,
    "tax_multiplier"       numeric(9, 7) NOT NULL,
    "is_pour"              boolean       NOT NULL,
    "site"                 text DEFAULT NULL,
    "notes"                text DEFAULT NULL
) WITHOUT OIDS;

CREATE TABLE drink_component
(
    "drink_name"           text          NOT NULL,
    "drink_version"        text          NOT NULL,
    "component_name"       text          NOT NULL,
    "units"                numeric(8, 2) NOT NULL,
    "component_unit_cost"  numeric(7, 4) NOT NULL,
    "cost_contribution"    numeric(6, 2) NOT NULL,
    "alcohol_percentage"   numeric(4, 2) NOT NULL,
    "alcohol_contribution" numeric(5, 2) NOT NULL,
    "display_units"        numeric(8, 2) NOT NULL,
    "display_unit_type"    unit_type     NOT NULL,
    "position"             int           NOT NULL,
    PRIMARY KEY ("drink_name", "drink_version", "component_name")
) WITHOUT OIDS;
CREATE INDEX "drink_components__component_name__idx" ON drink_component USING btree ("component_unit_cost");
ALTER TABLE drink_component
    ADD CONSTRAINT "drink_components__component_name_fk" FOREIGN KEY ("component_name") REFERENCES component ("name") ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT "drink_components__drink_name__drinks_version_fk" FOREIGN KEY ("drink_name", "drink_version") REFERENCES drink ("name", "version") ON DELETE CASCADE ON UPDATE CASCADE;
