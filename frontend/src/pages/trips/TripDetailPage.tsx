import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { tripApi } from '../../api/trips'
import { bookingApi } from '../../api/bookings'
import type { TripResponse } from '../../types'


export default function TripDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [trip, setTrip] = useState<TripResponse | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (id) {
      tripApi.getById(Number(id))
        .then(res => setTrip(res.data.data))
        .finally(() => setLoading(false))
    }
  }, [id])

  const handleBooking = async () => {
    if (!trip) return
    try {
      await bookingApi.create({ tripId: trip.id })
      alert('예약이 생성되었습니다!')
      navigate('/bookings')
    } catch {
      alert('예약 생성에 실패했습니다.')
    }
  }

  if (loading) return <div className="page">로딩 중...</div>
  if (!trip) return <div className="page">여행을 찾을 수 없습니다.</div>

  return (
    <div className="page trip-detail">
      <h2>{trip.title}</h2>
      <span className={`trip-status status-${trip.status}`}>{trip.status}</span>
      {trip.description && <p style={{ margin: '1rem 0', color: '#666' }}>{trip.description}</p>}
      <div style={{ marginTop: '1rem' }}>
        <div className="detail-row">
          <span className="detail-label">시작일</span>
          <span>{trip.startDate}</span>
        </div>
        <div className="detail-row">
          <span className="detail-label">종료일</span>
          <span>{trip.endDate}</span>
        </div>
        <div className="detail-row">
          <span className="detail-label">생성일</span>
          <span>{new Date(trip.createdAt).toLocaleDateString()}</span>
        </div>
      </div>
      <div className="detail-actions">
        {trip.status === 'PLANNED' && (
          <button onClick={handleBooking} className="btn btn-primary">예약하기</button>
        )}
        <button onClick={() => navigate('/trips')} className="btn btn-secondary">목록으로</button>
      </div>
    </div>
  )
}
