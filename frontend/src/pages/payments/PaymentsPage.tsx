import { Paper, Typography, Box } from '@mui/material'
import { Payment } from '@mui/icons-material'

export default function PaymentsPage() {
  return (
    <Paper sx={{ p: 3 }} elevation={0}>
      <Typography variant="h5" fontWeight={600} gutterBottom>결제 내역</Typography>
      <Box textAlign="center" py={5} color="#999">
        <Payment sx={{ fontSize: 48, mb: 1 }} />
        <Typography>결제 내역은 예약 페이지에서 확인할 수 있습니다.</Typography>
      </Box>
    </Paper>
  )
}
