import { Link } from 'react-router-dom'
import { Typography, Button, Box, Card, CardContent, Stack, Grid } from '@mui/material'
import { Flight, Hotel, Feed, Chat, LiveTv, VideoCall, TravelExplore, ArrowForward } from '@mui/icons-material'
import { useAppSelector } from '../store/hooks'

const features = [
  { icon: <Flight sx={{ fontSize: 32 }} />, title: '여행 계획', desc: '일정을 만들고 체계적으로 여행을 관리하세요', color: '#3b82f6', bg: '#eff6ff', path: '/trips' },
  { icon: <Hotel sx={{ fontSize: 32 }} />, title: '숙소 검색', desc: '전국의 숙소를 검색하고 비교해보세요', color: '#06b6d4', bg: '#ecfeff', path: '/accommodations' },
  { icon: <Feed sx={{ fontSize: 32 }} />, title: '여행 피드', desc: '나만의 여행 이야기를 사진과 함께 공유하세요', color: '#8b5cf6', bg: '#f5f3ff', path: '/feed' },
  { icon: <Chat sx={{ fontSize: 32 }} />, title: 'GPS 채팅', desc: '근처 여행자들과 실시간으로 소통하세요', color: '#22c55e', bg: '#f0fdf4', path: '/chat' },
  { icon: <LiveTv sx={{ fontSize: 32 }} />, title: '라이브 방송', desc: '여행지에서 실시간 스트리밍을 시작해보세요', color: '#ef4444', bg: '#fef2f2', path: '/streaming' },
  { icon: <VideoCall sx={{ fontSize: 32 }} />, title: '영상통화', desc: '크레딧으로 다른 여행자와 영상통화하세요', color: '#f97316', bg: '#fff7ed', path: '/subscription' },
]

export default function HomePage() {
  const { isAuthenticated } = useAppSelector((state) => state.auth)

  return (
    <Box>
      {/* Hero Section */}
      <Box sx={{
        textAlign: 'center', py: { xs: 6, md: 10 }, px: 2,
        borderRadius: 4, mb: 6,
        background: 'linear-gradient(135deg, #eff6ff 0%, #ecfeff 50%, #f0fdf4 100%)',
        position: 'relative', overflow: 'hidden',
      }}>
        <Box sx={{
          position: 'absolute', top: -40, right: -40, width: 200, height: 200,
          borderRadius: '50%', background: 'rgba(59,130,246,0.08)',
        }} />
        <Box sx={{
          position: 'absolute', bottom: -60, left: -20, width: 250, height: 250,
          borderRadius: '50%', background: 'rgba(6,182,212,0.06)',
        }} />

        <Box sx={{ position: 'relative', zIndex: 1 }}>
          <Box sx={{
            width: 72, height: 72, borderRadius: '20px', mx: 'auto', mb: 3,
            background: 'linear-gradient(135deg, #3b82f6 0%, #06b6d4 100%)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            boxShadow: '0 8px 32px rgba(59,130,246,0.3)',
          }}>
            <TravelExplore sx={{ color: 'white', fontSize: 36 }} />
          </Box>
          <Typography variant="h3" sx={{ mb: 1.5, color: '#1e293b', fontSize: { xs: '2rem', md: '3rem' } }}>
            여행의 모든 것,{' '}
            <Box component="span" sx={{ background: 'linear-gradient(135deg, #3b82f6, #06b6d4)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
              Trip
            </Box>
          </Typography>
          <Typography variant="h6" sx={{ color: 'text.secondary', fontWeight: 400, mb: 4, maxWidth: 500, mx: 'auto' }}>
            계획부터 예약, 공유, 실시간 소통까지<br />하나의 플랫폼에서 경험하세요
          </Typography>
          <Stack direction="row" spacing={2} justifyContent="center">
            {isAuthenticated ? (
              <>
                <Button component={Link} to="/trips" variant="contained" size="large" disableElevation endIcon={<ArrowForward />}
                  sx={{ px: 4, background: 'linear-gradient(135deg, #3b82f6, #2563eb)' }}>
                  여행 시작하기
                </Button>
                <Button component={Link} to="/feed" variant="outlined" size="large" sx={{ px: 4 }}>
                  피드 둘러보기
                </Button>
              </>
            ) : (
              <>
                <Button component={Link} to="/register" variant="contained" size="large" disableElevation endIcon={<ArrowForward />}
                  sx={{ px: 4, background: 'linear-gradient(135deg, #3b82f6, #2563eb)' }}>
                  무료로 시작하기
                </Button>
                <Button component={Link} to="/login" variant="outlined" size="large" sx={{ px: 4 }}>
                  로그인
                </Button>
              </>
            )}
          </Stack>
        </Box>
      </Box>

      {/* Feature Grid */}
      <Typography variant="h5" textAlign="center" sx={{ mb: 1 }}>주요 기능</Typography>
      <Typography variant="subtitle1" textAlign="center" sx={{ mb: 4 }}>여행에 필요한 모든 기능을 제공합니다</Typography>

      <Grid container spacing={2.5}>
        {features.map((f) => (
          <Grid size={{ xs: 12, sm: 6, md: 4 }} key={f.title}>
            <Card component={Link} to={isAuthenticated ? f.path : '/login'} elevation={0}
              sx={{ height: '100%', cursor: 'pointer', textDecoration: 'none', border: '1px solid #e2e8f0' }}>
              <CardContent sx={{ p: 3 }}>
                <Box sx={{ width: 56, height: 56, borderRadius: '14px', bgcolor: f.bg, display: 'flex', alignItems: 'center', justifyContent: 'center', color: f.color, mb: 2 }}>
                  {f.icon}
                </Box>
                <Typography variant="h6" sx={{ mb: 0.5, fontSize: '1.05rem' }}>{f.title}</Typography>
                <Typography variant="body2" color="text.secondary">{f.desc}</Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  )
}
