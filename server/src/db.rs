use jammdb::{DB, Tx};

use crate::error_handler::report_error;

pub fn tx(db: &DB, writable: bool) -> Option<Tx> {
    return match db.tx(writable) {
        Err(e) => {
            report_error(e.to_string());
            None
        }
        Ok(tx) => Some(tx)
    };
}

pub fn commit(tx: Tx) {
    if let Err(e) = tx.commit() {
        report_error(e.to_string());
    }
}
