import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Paper, Button, Typography, Box, Chip, Skeleton, Stack } from '@mui/material'
import { Add, Flight } from '@mui/icons-material'
import styled from 'styled-components'
import { tripApi } from '../../api/trips'
import type { TripResponse } from '../../types'

const TripCard = styled(Link)`
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.2rem;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  text-decoration: none;
  color: inherit;
  transition: box-shadow 0.2s;
  &:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
`

const statusColor: Record<string, 'info' | 'warning' | 'success' | 'error'> = {
  PLANNED: 'info', IN_PROGRESS: 'warning', COMPLETED: 'success', CANCELLED: 'error',
}

export default function TripsPage() {
  const { data: trips, isLoading } = useQuery({
    queryKey: ['trips', 'my'],
    queryFn: () => tripApi.getMyTrips().then(res => res.data.data || []),
  })

  return (
    <Paper sx={{ p: 3 }} elevation={0}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h5" fontWeight={600}>내 여행</Typography>
        <Button component={Link} to="/trips/new" variant="contained" startIcon={<Add />}>여행 만들기</Button>
      </Box>
      {isLoading ? (
        <Stack spacing={1}>{[1,2,3].map(i => <Skeleton key={i} height={70} variant="rounded" />)}</Stack>
      ) : !trips?.length ? (
        <Box textAlign="center" py={5} color="#999">
          <Flight sx={{ fontSize: 48, mb: 1 }} />
          <Typography>아직 여행이 없습니다. 새 여행을 만들어보세요!</Typography>
        </Box>
      ) : (
        <Stack spacing={1.5}>
          {trips.map((trip: TripResponse) => (
            <TripCard key={trip.id} to={`/trips/${trip.id}`}>
              <div>
                <Typography fontWeight={600}>{trip.title}</Typography>
                <Typography variant="body2" color="text.secondary">{trip.startDate} ~ {trip.endDate}</Typography>
              </div>
              <Chip label={trip.status} color={statusColor[trip.status]} size="small" />
            </TripCard>
          ))}
        </Stack>
      )}
    </Paper>
  )
}
