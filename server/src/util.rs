use std::cmp::Ordering;
use std::collections::BinaryHeap;
use rocket::{Data, Request, response, Response};
use rocket::data::{FromData, Outcome, ToByteUnit};
use rocket::http::Status;
use rocket::response::Responder;

/// Utility data guard and response type for empty bodies.
pub struct Empty {}

#[rocket::async_trait]
impl<'r> FromData<'r> for Empty {
    type Error = ();

    async fn from_data(_: &'r Request<'_>, data: Data<'r>) -> Outcome<'r, Self> {
        let res = data.open(1.bytes()).into_bytes().await;
        if let Ok(bytes) = res {
            if bytes.len() == 0 {
                return Outcome::Success(Empty {});
            }
        }
        Outcome::Error((Status::UnprocessableEntity, ()))
    }
}

impl<'r> Responder<'r, 'static> for Empty {
    fn respond_to(self, _: &'r Request<'_>) -> response::Result<'static> {
        Response::build()
            .status(Status::NoContent)
            .ok()
    }
}

/// Utility struct for sorted (decreasing) and limited vectors
pub struct SortedLimitedVec<K: Ord, V> {
    inner: BinaryHeap<SLVecEntry<K, V>>,
    limit: usize,
    get_key: fn(&V) -> K,
}

impl<K: Ord, V> SortedLimitedVec<K, V> {
    pub fn new(limit: usize, get_key: fn(&V) -> K) -> Self {
        SortedLimitedVec {
            inner: BinaryHeap::new(),
            limit,
            get_key,
        }
    }

    pub fn push(&mut self, value: V) {
        self.inner.push(SLVecEntry {
            key: (self.get_key)(&value),
            value,
        });
        self.inner.shrink_to(self.limit);
    }

    pub fn to_vec(self) -> Vec<V> {
        self.inner.into_vec().into_iter().map(|v| v.value).collect()
    }
}

struct SLVecEntry<K: Ord, V> {
    key: K,
    value: V,
}

impl<K: Ord, V> Eq for SLVecEntry<K, V> {}

impl<K: Ord, V> PartialEq<Self> for SLVecEntry<K, V> {
    fn eq(&self, other: &Self) -> bool {
        self.key.eq(&other.key)
    }
}

impl<K: Ord, V> PartialOrd<Self> for SLVecEntry<K, V> {
    fn partial_cmp(&self, other: &Self) -> Option<Ordering> {
        self.key.partial_cmp(&other.key)
    }
}

impl<K: Ord, V> Ord for SLVecEntry<K, V> {
    fn cmp(&self, other: &Self) -> Ordering {
        self.key.cmp(&other.key)
    }
}
