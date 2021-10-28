ALTER TABLE public.users ADD COLUMN time_of_session_expiration timestamp with time zone;
UPDATE public.users SET time_of_session_expiration = CURRENT_TIMESTAMP;
ALTER TABLE public.users ALTER COLUMN time_of_session_expiration SET NOT NULL;

ALTER TABLE public.users ADD COLUMN change_password_date timestamp with time zone;
UPDATE public.users SET change_password_date = CURRENT_TIMESTAMP;
ALTER TABLE public.users ALTER COLUMN change_password_date SET NOT NULL;