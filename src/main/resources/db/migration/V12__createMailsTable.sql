CREATE SEQUENCE IF NOT EXISTS public.mail_sequence
    INCREMENT 1
    START 1000
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

CREATE TABLE public.mails
(
    id BIGINT NOT NULL,
    created_at TIMESTAMP without time zone NOT NULL,
    updated_at TIMESTAMP without time zone NOT NULL,
    email_recipient character varying(255),
    subject character varying(255),
    text TEXT,
    is_html_content BOOLEAN NOT NULL,
    CONSTRAINT mails_pkey PRIMARY KEY (id)
);
