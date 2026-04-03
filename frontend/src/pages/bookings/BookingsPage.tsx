import { useEffect, useState } from 'react'
import { bookingApi } from '../../api/bookings'
import { paymentApi } from '../../api/payments'
import type { BookingResponse } from '../../types'


export default function BookingsPage() {
  const [bookings, setBookings] = useState<BookingResponse[]>([])
  const [loading, setLoading] = useState(true)

  const fetchBookings = () => {
    bookingApi.getMyBookings()
      .then(res => setBookings(res.data.data || []))
      .finally(() => setLoading(false))
  }

  useEffect(() => { fetchBookings() }, [])

  const handleCancel = async (id: number) => {
    if (!confirm('예약을 취소하시겠습니까?')) return
    await bookingApi.cancel(id)
    fetchBookings()
  }

  const handlePay = async (booking: BookingResponse) => {
    const amount = prompt('결제 금액을 입력하세요')
    if (!amount) return
    try {
      const res = await paymentApi.create({ bookingId: booking.id, amount: Number(amount) })
      const paymentId = res.data.data?.id
      if (paymentId) {
        await paymentApi.complete(paymentId)
        alert('결제가 완료되었습니다!')
        fetchBookings()
      }
    } catch {
      alert('결제에 실패했습니다.')
    }
  }

  if (loading) return <div className="page">로딩 중...</div>

  return (
    <div className="page">
      <h2 style={{ marginBottom: '1.5rem' }}>내 예약</h2>
      {bookings.length === 0 ? (
        <p style={{ color: '#999', textAlign: 'center', padding: '2rem' }}>예약이 없습니다.</p>
      ) : (
        <div className="trip-list">
          {bookings.map(b => (
            <div key={b.id} className="trip-card" style={{ cursor: 'default' }}>
              <div className="trip-info">
                <h3>예약 #{b.id}</h3>
                <p>여행 #{b.tripId} | {new Date(b.bookedAt).toLocaleDateString()}</p>
              </div>
              <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                <span className={`trip-status status-${b.status}`}>{b.status}</span>
                {b.status === 'PENDING' && (
                  <>
                    <button onClick={() => handlePay(b)} className="btn btn-primary" style={{ padding: '0.3rem 0.8rem', fontSize: '0.85rem' }}>결제</button>
                    <button onClick={() => handleCancel(b.id)} className="btn btn-danger" style={{ padding: '0.3rem 0.8rem', fontSize: '0.85rem' }}>취소</button>
                  </>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
