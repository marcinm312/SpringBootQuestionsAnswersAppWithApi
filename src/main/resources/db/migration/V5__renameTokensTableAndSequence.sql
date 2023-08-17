ALTER TABLE public.tokens RENAME TO activation_tokens;
ALTER SEQUENCE public.token_id_sequence RENAME TO activation_token_sequence;
