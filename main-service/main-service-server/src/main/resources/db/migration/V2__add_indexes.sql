-- Indexes for better performance
CREATE INDEX IF NOT EXISTS idx_events_category_id ON events(category_id);
CREATE INDEX IF NOT EXISTS idx_events_initiator_id ON events(initiator_id);
CREATE INDEX IF NOT EXISTS idx_events_event_date ON events(event_date);
CREATE INDEX IF NOT EXISTS idx_events_state ON events(state);
CREATE INDEX IF NOT EXISTS idx_participation_requests_event_id ON participation_requests(event_id);
CREATE INDEX IF NOT EXISTS idx_participation_requests_requester_id ON participation_requests(requester_id);
CREATE INDEX IF NOT EXISTS idx_compilations_pinned ON compilations(pinned);