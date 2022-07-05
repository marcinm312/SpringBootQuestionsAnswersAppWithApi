CREATE SEQUENCE public.user_sequence
    INCREMENT 50
    START 1000
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

CREATE SEQUENCE public.token_id_sequence
    INCREMENT 50
    START 1000
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

CREATE SEQUENCE public.question_sequence
    INCREMENT 50
    START 1000
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

CREATE SEQUENCE public.answer_sequence
    INCREMENT 50
    START 1000
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;


CREATE TABLE public.users
(
    id bigint NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    email character varying(255),
    first_name character varying(255),
    is_enabled boolean NOT NULL,
    last_name character varying(255),
    password character varying(255),
    role character varying(255),
    username character varying(50),
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT users_unique_username UNIQUE (username)
);

CREATE TABLE public.tokens
(
    id bigint NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    value character varying(255),
    user_id bigint,
    CONSTRAINT tokens_pkey PRIMARY KEY (id),
    CONSTRAINT tokens_fkey_user_id FOREIGN KEY (user_id)
        REFERENCES public.users (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

CREATE TABLE public.questions
(
    id bigint NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    description text,
    title character varying(100),
    user_id bigint NOT NULL,
    CONSTRAINT questions_pkey PRIMARY KEY (id),
    CONSTRAINT questions_fkey_user_id FOREIGN KEY (user_id)
        REFERENCES public.users (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);
	
CREATE TABLE public.answers
(
    id bigint NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    text text,
    question_id bigint NOT NULL,
    user_id bigint NOT NULL,
    CONSTRAINT answers_pkey PRIMARY KEY (id),
    CONSTRAINT answers_fkey_question_id FOREIGN KEY (question_id)
        REFERENCES public.questions (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE,
    CONSTRAINT answers_fkey_user_id FOREIGN KEY (user_id)
        REFERENCES public.users (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);