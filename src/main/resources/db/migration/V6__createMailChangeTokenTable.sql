CREATE SEQUENCE public.mail_change_token_sequence
    INCREMENT 50
    START 1000
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

CREATE TABLE public.mail_change_tokens
(
    id bigint NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    value character varying(255) NOT NULL,
    new_email character varying(255) NOT NULL,
    user_id bigint,
    CONSTRAINT mail_change_tokens_pkey PRIMARY KEY (id),
    CONSTRAINT mail_change_tokens_fkey_user_id FOREIGN KEY (user_id)
        REFERENCES public.users (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);
