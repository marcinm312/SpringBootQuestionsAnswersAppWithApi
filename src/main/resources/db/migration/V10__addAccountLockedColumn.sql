ALTER TABLE public.users ADD COLUMN is_account_non_locked boolean;
UPDATE public.users SET is_account_non_locked = true;
ALTER TABLE public.users ALTER COLUMN is_account_non_locked SET NOT NULL;