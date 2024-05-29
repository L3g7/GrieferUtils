use std::collections::HashMap;

pub fn get_with_least_avg_deviation<K: PartialEq, V: DeviatingRef>(data: &HashMap<K, V>) -> Option<&V> {
    let mut considered_value = None;
    let mut considered_avg = u64::MAX;

    for (k, v) in data {
        let mut dev_sum = 0u64;
        let mut dev_count = 0u64;

        // Compute average deviation
        for (k2, v2) in data {
            if k != k2 {
                dev_sum += v.deviation_to(v2);
                dev_count += 1;
            }
        }

        if dev_count == 0 {
            // Only one value in entries
            return considered_value;
        }

        // Choose value with less avg. deviation
        if dev_count != 0 {
            let avg = dev_sum / dev_count;
            if avg < considered_avg {
                considered_avg = avg;
            }
        } else {
            considered_value = Some(v)
            // keep considered_deviation at max value
        }
    }

    return considered_value;
}

/// Deviation calculation with references
pub trait DeviatingRef<Other = Self> {
    fn deviation_to(&self, other: &Other) -> u64;
}
