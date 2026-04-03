import { Link } from 'react-router-dom'
import { Typography, Button, Stack, Box } from '@mui/material'
import { Flight, Feed, Chat } from '@mui/icons-material'
import { useAppSelector } from '../store/hooks'

export default function HomePage() {
  const { isAuthenticated } = useAppSelector((state) => state.auth)

  return (
    <Box textAlign="center" py={8}>
      <Flight sx={{ fontSize: 64, color: 'primary.main', mb: 2 }} />
      <Typography variant="h3" fontWeight={700} color="primary" gutterBottom>Trip</Typography>
      <Typography variant="h6" color="text.secondary" gutterBottom>
        여행을 계획하고, 예약하고, 공유하세요.
      </Typography>
      <Stack direction="row" spacing={2} justifyContent="center" sx={{ mt: 4 }}>
        {isAuthenticated ? (
          <>
            <Button component={Link} to="/trips" variant="contained" size="large" startIcon={<Flight />}>내 여행</Button>
            <Button component={Link} to="/feed" variant="outlined" size="large" startIcon={<Feed />}>피드</Button>
            <Button component={Link} to="/chat" variant="outlined" size="large" startIcon={<Chat />}>채팅</Button>
          </>
        ) : (
          <>
            <Button component={Link} to="/login" variant="contained" size="large">로그인</Button>
            <Button component={Link} to="/register" variant="outlined" size="large">회원가입</Button>
          </>
        )}
      </Stack>
    </Box>
  )
}
