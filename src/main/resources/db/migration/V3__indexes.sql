ALTER TABLE public.users ALTER COLUMN username SET NOT NULL;
CREATE UNIQUE INDEX username_idx ON public.users (username);