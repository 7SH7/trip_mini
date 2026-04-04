import { Link } from 'react-router-dom'
import { Typography, Button, Box, Card, CardContent, Stack, Grid } from '@mui/material'
import { Flight, Hotel, Feed, Chat, LiveTv, VideoCall, TravelExplore, ArrowForward } from '@mui/icons-material'
import { useAppSelector } from '../store/hooks'
import { motion } from 'framer-motion'
import { keyframes } from '@emotion/react'
import styled from '@emotion/styled'

const floatAnim = keyframes`
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-14px); }
`

const HeroOrb = styled.div<{ size: number; top: string; left: string; delay: string; color: string }>`
  position: absolute;
  width: ${p => p.size}px;
  height: ${p => p.size}px;
  border-radius: 50%;
  background: ${p => p.color};
  top: ${p => p.top};
  left: ${p => p.left};
  animation: ${floatAnim} 6s ease-in-out ${p => p.delay} infinite;
  filter: blur(1px);
  pointer-events: none;
`

const features = [
  { icon: <Flight sx={{ fontSize: 28 }} />, title: '여행 계획', desc: '팟을 만들어 친구와 함께 일정을 관리하세요', color: '#3b82f6', bg: 'linear-gradient(135deg, #eff6ff, #dbeafe)', path: '/trips' },
  { icon: <Hotel sx={{ fontSize: 28 }} />, title: '숙소 검색', desc: '전국의 숙소를 검색하고 여행에 추가하세요', color: '#06b6d4', bg: 'linear-gradient(135deg, #ecfeff, #cffafe)', path: '/accommodations' },
  { icon: <Feed sx={{ fontSize: 28 }} />, title: '여행 피드', desc: '나만의 여행 이야기를 사진과 함께 공유하세요', color: '#8b5cf6', bg: 'linear-gradient(135deg, #f5f3ff, #ede9fe)', path: '/feed' },
  { icon: <Chat sx={{ fontSize: 28 }} />, title: 'GPS 채팅', desc: '근처 여행자들과 실시간으로 소통하세요', color: '#22c55e', bg: 'linear-gradient(135deg, #f0fdf4, #dcfce7)', path: '/chat' },
  { icon: <LiveTv sx={{ fontSize: 28 }} />, title: '라이브 방송', desc: '여행지에서 실시간 스트리밍을 시작해보세요', color: '#ef4444', bg: 'linear-gradient(135deg, #fef2f2, #fee2e2)', path: '/streaming' },
  { icon: <VideoCall sx={{ fontSize: 28 }} />, title: '영상통화', desc: '크레딧으로 다른 여행자와 영상통화하세요', color: '#f97316', bg: 'linear-gradient(135deg, #fff7ed, #ffedd5)', path: '/subscription' },
]

const container = { hidden: {}, show: { transition: { staggerChildren: 0.08 } } }
const item = { hidden: { opacity: 0, y: 24 }, show: { opacity: 1, y: 0, transition: { duration: 0.5, ease: [0.25, 0.1, 0.25, 1] as const } } }

export default function HomePage() {
  const { isAuthenticated } = useAppSelector((state) => state.auth)

  return (
    <Box>
      {/* Hero */}
      <Box sx={{
        textAlign: 'center', py: { xs: 7, md: 11 }, px: 3, borderRadius: 5, mb: 7,
        background: 'linear-gradient(160deg, #eff6ff 0%, #f0fdfa 40%, #fdf4ff 100%)',
        position: 'relative', overflow: 'hidden',
      }}>
        <HeroOrb size={180} top="-30px" left="-40px" delay="0s" color="rgba(59,130,246,0.07)" />
        <HeroOrb size={120} top="60%" left="85%" delay="1s" color="rgba(139,92,246,0.06)" />
        <HeroOrb size={200} top="70%" left="10%" delay="2s" color="rgba(6,182,212,0.05)" />

        <motion.div initial={{ opacity: 0, y: 30 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.7, ease: [0.25, 0.1, 0.25, 1] as const }}
          style={{ position: 'relative', zIndex: 1 }}>
          <Box sx={{
            width: 80, height: 80, borderRadius: '22px', mx: 'auto', mb: 3,
            background: 'linear-gradient(135deg, #3b82f6 0%, #8b5cf6 50%, #06b6d4 100%)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            boxShadow: '0 12px 40px rgba(59,130,246,0.35)',
            animation: `${floatAnim} 4s ease-in-out infinite`,
          }}>
            <TravelExplore sx={{ color: 'white', fontSize: 40 }} />
          </Box>
          <Typography variant="h3" sx={{ mb: 1.5, color: '#0f172a', fontSize: { xs: '2.2rem', md: '3.2rem' }, lineHeight: 1.2 }}>
            여행의 모든 것,{' '}
            <Box component="span" sx={{
              background: 'linear-gradient(135deg, #3b82f6, #8b5cf6, #06b6d4)',
              backgroundSize: '200% auto',
              WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent',
              animation: 'gradientShift 4s ease infinite',
            }}>
              Trip
            </Box>
          </Typography>
          <Typography variant="h6" sx={{ color: '#64748b', fontWeight: 400, mb: 5, maxWidth: 480, mx: 'auto', lineHeight: 1.7 }}>
            계획부터 예약, 공유, 실시간 소통까지<br />하나의 플랫폼에서 경험하세요
          </Typography>
          <Stack direction="row" spacing={2} justifyContent="center">
            {isAuthenticated ? (
              <>
                <Button component={Link} to="/trips" variant="contained" size="large" disableElevation endIcon={<ArrowForward />}
                  sx={{ px: 4, py: 1.5, background: 'linear-gradient(135deg, #3b82f6, #8b5cf6)', borderRadius: 3, fontSize: '1rem' }}>
                  여행 시작하기
                </Button>
                <Button component={Link} to="/feed" variant="outlined" size="large"
                  sx={{ px: 4, py: 1.5, borderRadius: 3, borderWidth: 2, fontSize: '1rem', '&:hover': { borderWidth: 2 } }}>
                  피드 둘러보기
                </Button>
              </>
            ) : (
              <>
                <Button component={Link} to="/register" variant="contained" size="large" disableElevation endIcon={<ArrowForward />}
                  sx={{ px: 4, py: 1.5, background: 'linear-gradient(135deg, #3b82f6, #8b5cf6)', borderRadius: 3, fontSize: '1rem' }}>
                  무료로 시작하기
                </Button>
                <Button component={Link} to="/login" variant="outlined" size="large"
                  sx={{ px: 4, py: 1.5, borderRadius: 3, borderWidth: 2, fontSize: '1rem', '&:hover': { borderWidth: 2 } }}>
                  로그인
                </Button>
              </>
            )}
          </Stack>
        </motion.div>
      </Box>

      {/* Features */}
      <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: 0.3, duration: 0.5 }}>
        <Typography variant="h4" textAlign="center" sx={{ mb: 0.5 }}>주요 기능</Typography>
        <Typography variant="body1" textAlign="center" color="text.secondary" sx={{ mb: 5 }}>여행에 필요한 모든 기능을 제공합니다</Typography>
      </motion.div>

      <motion.div variants={container} initial="hidden" animate="show">
        <Grid container spacing={2.5}>
          {features.map((f) => (
            <Grid size={{ xs: 12, sm: 6, md: 4 }} key={f.title}>
              <motion.div variants={item}>
                <Card component={Link} to={isAuthenticated ? f.path : '/login'} elevation={0}
                  sx={{
                    height: '100%', cursor: 'pointer', border: '1px solid transparent',
                    transition: 'all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1)',
                    '&:hover': { borderColor: `${f.color}40`, boxShadow: `0 8px 30px ${f.color}15`, transform: 'translateY(-4px)' },
                  }}>
                  <CardContent sx={{ p: 3 }}>
                    <Box sx={{
                      width: 56, height: 56, borderRadius: '16px', background: f.bg,
                      display: 'flex', alignItems: 'center', justifyContent: 'center',
                      color: f.color, mb: 2.5, transition: 'transform 0.3s',
                      '.MuiCard-root:hover &': { transform: 'scale(1.1)' },
                    }}>
                      {f.icon}
                    </Box>
                    <Typography variant="h6" sx={{ mb: 0.5, fontSize: '1.1rem', color: '#0f172a' }}>{f.title}</Typography>
                    <Typography variant="body2" sx={{ color: '#64748b', lineHeight: 1.6 }}>{f.desc}</Typography>
                  </CardContent>
                </Card>
              </motion.div>
            </Grid>
          ))}
        </Grid>
      </motion.div>
    </Box>
  )
}
