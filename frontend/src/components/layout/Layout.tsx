import { Outlet, Link, useNavigate, useLocation } from 'react-router-dom'
import {
  AppBar, Toolbar, Button, Box, Typography, Container, Badge, IconButton,
  Drawer, List, ListItemButton, ListItemIcon, ListItemText, Avatar, Divider,
  Stack, useMediaQuery, useTheme, Tooltip
} from '@mui/material'
import {
  Flight, Chat, Feed, Hotel, Subscriptions, LiveTv, Notifications,
  Menu as MenuIcon, Logout, Payment, BookOnline, Person, Close, TravelExplore
} from '@mui/icons-material'
import { useAppDispatch, useAppSelector } from '../../store/hooks'
import { logout } from '../../store/authSlice'
import { useQuery } from '@tanstack/react-query'
import { notificationApi } from '../../api/notifications'
import { useState } from 'react'

const DRAWER_WIDTH = 260

const navItems = [
  { label: '내 여행', path: '/trips', icon: <Flight /> },
  { label: '숙소 검색', path: '/accommodations', icon: <Hotel /> },
  { label: '예약 내역', path: '/bookings', icon: <BookOnline /> },
  { label: '결제 내역', path: '/payments', icon: <Payment /> },
  { label: '피드', path: '/feed', icon: <Feed /> },
  { label: '채팅', path: '/chat', icon: <Chat /> },
  { label: '라이브', path: '/streaming', icon: <LiveTv /> },
  { label: '구독 관리', path: '/subscription', icon: <Subscriptions /> },
]

export default function Layout() {
  const { isAuthenticated, user } = useAppSelector((state) => state.auth)
  const dispatch = useAppDispatch()
  const navigate = useNavigate()
  const location = useLocation()
  const theme = useTheme()
  const isMobile = useMediaQuery(theme.breakpoints.down('md'))
  const [drawerOpen, setDrawerOpen] = useState(false)

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

  const drawerContent = (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Box sx={{ p: 2.5, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Stack direction="row" spacing={1.5} alignItems="center" component={Link} to="/">
          <Box sx={{
            width: 36, height: 36, borderRadius: '10px',
            background: 'linear-gradient(135deg, #3b82f6 0%, #06b6d4 100%)',
            display: 'flex', alignItems: 'center', justifyContent: 'center'
          }}>
            <TravelExplore sx={{ color: 'white', fontSize: 20 }} />
          </Box>
          <Typography variant="h6" fontWeight={800} sx={{ background: 'linear-gradient(135deg, #3b82f6, #06b6d4)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
            Trip
          </Typography>
        </Stack>
        {isMobile && (
          <IconButton onClick={() => setDrawerOpen(false)} size="small"><Close /></IconButton>
        )}
      </Box>
      <Divider />

      <List sx={{ flex: 1, px: 1.5, py: 1 }}>
        {navItems.map((item) => {
          const isActive = location.pathname === item.path || location.pathname.startsWith(item.path + '/')
          return (
            <ListItemButton
              key={item.path}
              component={Link}
              to={item.path}
              onClick={() => isMobile && setDrawerOpen(false)}
              sx={{
                borderRadius: '10px', mb: 0.5, py: 1,
                bgcolor: isActive ? 'primary.main' : 'transparent',
                color: isActive ? 'white' : 'text.secondary',
                '&:hover': { bgcolor: isActive ? 'primary.dark' : '#f1f5f9' },
                '& .MuiListItemIcon-root': { color: isActive ? 'white' : 'text.secondary', minWidth: 40 },
              }}
            >
              <ListItemIcon>{item.icon}</ListItemIcon>
              <ListItemText primary={item.label} primaryTypographyProps={{ fontWeight: isActive ? 600 : 500, fontSize: '0.9rem' }} />
            </ListItemButton>
          )
        })}
      </List>

      <Divider />
      <Box sx={{ p: 2 }}>
        <Stack direction="row" spacing={1.5} alignItems="center" sx={{ mb: 1.5, px: 1 }}>
          <Avatar sx={{ width: 36, height: 36, bgcolor: 'primary.main', fontSize: 14, fontWeight: 700 }}>
            {user?.name?.charAt(0) || 'U'}
          </Avatar>
          <Box sx={{ minWidth: 0 }}>
            <Typography variant="body2" fontWeight={600} noWrap>{user?.name || 'User'}</Typography>
            <Typography variant="caption" color="text.secondary" noWrap>{user?.email || ''}</Typography>
          </Box>
        </Stack>
        <Button fullWidth variant="outlined" color="inherit" startIcon={<Logout />} onClick={handleLogout}
          sx={{ borderColor: '#e2e8f0', color: 'text.secondary', '&:hover': { bgcolor: '#fef2f2', borderColor: '#fca5a5', color: '#ef4444' } }}>
          로그아웃
        </Button>
      </Box>
    </Box>
  )

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
      {isAuthenticated && (
        <>
          {!isMobile && (
            <Drawer variant="permanent" sx={{
              width: DRAWER_WIDTH, flexShrink: 0,
              '& .MuiDrawer-paper': { width: DRAWER_WIDTH, borderRight: '1px solid #e2e8f0', bgcolor: 'white' },
            }}>
              {drawerContent}
            </Drawer>
          )}
          {isMobile && (
            <Drawer open={drawerOpen} onClose={() => setDrawerOpen(false)} sx={{
              '& .MuiDrawer-paper': { width: DRAWER_WIDTH },
            }}>
              {drawerContent}
            </Drawer>
          )}
        </>
      )}

      <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column', minWidth: 0 }}>
        <AppBar position="sticky" elevation={0} sx={{
          bgcolor: 'rgba(255,255,255,0.8)', backdropFilter: 'blur(12px)',
          color: 'text.primary', borderBottom: '1px solid #e2e8f0',
        }}>
          <Toolbar sx={{ justifyContent: 'space-between' }}>
            <Stack direction="row" spacing={1} alignItems="center">
              {isAuthenticated && isMobile && (
                <IconButton onClick={() => setDrawerOpen(true)}><MenuIcon /></IconButton>
              )}
              {!isAuthenticated && (
                <Stack direction="row" spacing={1.5} alignItems="center" component={Link} to="/">
                  <Box sx={{
                    width: 32, height: 32, borderRadius: '8px',
                    background: 'linear-gradient(135deg, #3b82f6 0%, #06b6d4 100%)',
                    display: 'flex', alignItems: 'center', justifyContent: 'center'
                  }}>
                    <TravelExplore sx={{ color: 'white', fontSize: 18 }} />
                  </Box>
                  <Typography variant="h6" fontWeight={800} sx={{ background: 'linear-gradient(135deg, #3b82f6, #06b6d4)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
                    Trip
                  </Typography>
                </Stack>
              )}
            </Stack>

            <Stack direction="row" spacing={1} alignItems="center">
              {isAuthenticated ? (
                <>
                  <Tooltip title="알림">
                    <IconButton component={Link} to="/notifications">
                      <Badge badgeContent={unreadCount || 0} color="error" sx={{ '& .MuiBadge-badge': { fontWeight: 700 } }}>
                        <Notifications sx={{ color: location.pathname === '/notifications' ? 'primary.main' : 'text.secondary' }} />
                      </Badge>
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="내 프로필">
                    <IconButton component={Link} to="/trips" size="small">
                      <Avatar sx={{ width: 32, height: 32, bgcolor: 'primary.light', fontSize: 13, fontWeight: 700 }}>
                        {user?.name?.charAt(0) || <Person />}
                      </Avatar>
                    </IconButton>
                  </Tooltip>
                </>
              ) : (
                <>
                  <Button component={Link} to="/login" sx={{ color: 'text.primary' }}>로그인</Button>
                  <Button component={Link} to="/register" variant="contained" disableElevation>회원가입</Button>
                </>
              )}
            </Stack>
          </Toolbar>
        </AppBar>

        <Container maxWidth="lg" sx={{ py: 4, flex: 1 }}>
          <Outlet />
        </Container>
      </Box>
    </Box>
  )
}
