ALTER TABLE public.activation_tokens DROP CONSTRAINT tokens_fkey_user_id;
ALTER TABLE public.activation_tokens ADD CONSTRAINT tokens_fkey_user_id FOREIGN KEY (user_id)
    REFERENCES public.users (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE;