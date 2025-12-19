-- Composite index for findByBrandAndState queries
-- Also covers findByBrand queries (leftmost column)
CREATE INDEX idx_devices_brand_state ON devices(brand, state);

-- Drop redundant single-column brand index (composite covers it)
DROP INDEX idx_devices_brand;
