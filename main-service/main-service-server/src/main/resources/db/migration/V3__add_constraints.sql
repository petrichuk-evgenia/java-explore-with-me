-- Additional constraints for data integrity
ALTER TABLE events ALTER COLUMN participant_limit SET DEFAULT 0;
ALTER TABLE events ALTER COLUMN request_moderation SET DEFAULT TRUE;
ALTER TABLE events ALTER COLUMN paid SET DEFAULT FALSE;

ALTER TABLE participation_requests ALTER COLUMN status SET DEFAULT 'PENDING';
ALTER TABLE compilations ALTER COLUMN pinned SET DEFAULT FALSE;