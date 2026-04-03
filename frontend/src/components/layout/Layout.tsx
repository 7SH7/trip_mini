import { Outlet, Link, useNavigate } from 'react-router-dom'
import { AppBar, Toolbar, Button, Box, Typography, Container, Badge, IconButton } from '@mui/material'
import { Flight, Chat, Feed, Hotel, Subscriptions, LiveTv, Notifications } from '@mui/icons-material'
import styled from 'styled-components'
import { useAppDispatch, useAppSelector } from '../../store/hooks'
import { logout } from '../../store/authSlice'
import { useQuery } from '@tanstack/react-query'
import { notificationApi } from '../../api/notifications'

const NavLink = styled(Link)`
  text-decoration: none;
  color: inherit;
`

export default function Layout() {
  const { isAuthenticated } = useAppSelector((state) => state.auth)
  const dispatch = useAppDispatch()
  const navigate = useNavigate()

  const { data: unreadCount } = useQuery({
    queryKey: ['notifications', 'unread'],
    queryFn: () => notificationApi.getUnreadCount().then(r => r.data.data?.count ?? 0),
    enabled: isAuthenticated,
    refetchInterval: 30000,
  })

  const handleLogout = () => {
    dispatch(logout())
    navigate('/login')
  }

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#f5f7fa' }}>
      <AppBar position="sticky" sx={{ bgcolor: 'white', color: '#333' }} elevation={1}>
        <Toolbar sx={{ justifyContent: 'space-between' }}>
          <NavLink to="/">
            <Typography variant="h6" fontWeight={700} color="primary">Trip</Typography>
          </NavLink>
          <Box sx={{ display: 'flex', gap: 0.5, alignItems: 'center' }}>
            {isAuthenticated ? (
              <>
                <Button component={Link} to="/trips" startIcon={<Flight />} size="small">여행</Button>
                <Button component={Link} to="/accommodations" startIcon={<Hotel />} size="small">숙소</Button>
                <Button component={Link} to="/feed" startIcon={<Feed />} size="small">피드</Button>
                <Button component={Link} to="/chat" startIcon={<Chat />} size="small">채팅</Button>
                <Button component={Link} to="/streaming" startIcon={<LiveTv />} size="small">스트리밍</Button>
                <Button component={Link} to="/subscription" startIcon={<Subscriptions />} size="small">구독</Button>
                <IconButton component={Link} to="/notifications" size="small">
                  <Badge badgeContent={unreadCount || 0} color="error">
                    <Notifications />
                  </Badge>
                </IconButton>
                <Button variant="outlined" size="small" onClick={handleLogout}>로그아웃</Button>
              </>
            ) : (
              <>
                <Button component={Link} to="/login">로그인</Button>
                <Button component={Link} to="/register" variant="contained">회원가입</Button>
              </>
            )}
          </Box>
        </Toolbar>
      </AppBar>
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Outlet />
      </Container>
    </Box>
  )
}
