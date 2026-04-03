declare global {
  interface Window {
    IMP?: {
      init: (merchantId: string) => void
      request_pay: (params: Record<string, unknown>, callback: (response: PortOneResponse) => void) => void
    }
  }
}

interface PortOneResponse {
  success: boolean
  imp_uid: string
  merchant_uid: string
  error_msg?: string
}

const MERCHANT_ID = import.meta.env.VITE_PORTONE_MERCHANT_ID || 'imp00000000'

let scriptLoaded = false

function loadPortoneScript(): Promise<void> {
  if (scriptLoaded) return Promise.resolve()
  return new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.src = 'https://cdn.iamport.kr/v1/iamport.js'
    script.onload = () => {
      scriptLoaded = true
      window.IMP?.init(MERCHANT_ID)
      resolve()
    }
    script.onerror = reject
    document.head.appendChild(script)
  })
}

export async function requestPayment(amount: number, name: string): Promise<string> {
  await loadPortoneScript()

  return new Promise((resolve, reject) => {
    if (!window.IMP) {
      reject(new Error('PortOne SDK not loaded'))
      return
    }

    const merchantUid = `trip_${Date.now()}`

    window.IMP.request_pay(
      {
        pg: 'html5_inicis',
        pay_method: 'card',
        merchant_uid: merchantUid,
        name,
        amount,
      },
      (response: PortOneResponse) => {
        if (response.success) {
          resolve(response.imp_uid)
        } else {
          reject(new Error(response.error_msg || '결제에 실패했습니다.'))
        }
      }
    )
  })
}
