import { Outlet, Link, useNavigate } from 'react-router-dom'
import { AppBar, Toolbar, Button, Box, Typography, Container } from '@mui/material'
import { Flight, Chat, Feed, BookOnline } from '@mui/icons-material'
import styled from 'styled-components'
import { useAppDispatch, useAppSelector } from '../../store/hooks'
import { logout } from '../../store/authSlice'

const NavLink = styled(Link)`
  text-decoration: none;
  color: inherit;
  display: flex;
  align-items: center;
  gap: 4px;
`

export default function Layout() {
  const { isAuthenticated } = useAppSelector((state) => state.auth)
  const dispatch = useAppDispatch()
  const navigate = useNavigate()

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
          <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
            {isAuthenticated ? (
              <>
                <Button component={Link} to="/trips" startIcon={<Flight />}>여행</Button>
                <Button component={Link} to="/bookings" startIcon={<BookOnline />}>예약</Button>
                <Button component={Link} to="/feed" startIcon={<Feed />}>피드</Button>
                <Button component={Link} to="/chat" startIcon={<Chat />}>채팅</Button>
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
