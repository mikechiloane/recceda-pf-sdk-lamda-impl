# PayFast Sandbox Localhost Issue

## 🚫 The Problem

PayFast sandbox **cannot** reach `localhost` endpoints because:

1. **PayFast servers are on the internet** → Your laptop is on a private network
2. **`localhost` is not publicly accessible** → Only exists on your machine
3. **Firewall/NAT blocks external access** → Your router blocks incoming connections

```
[PayFast Servers] ❌ → [Internet] → [Your Router/Firewall] ❌ → [localhost:3001]
```

## ✅ Solutions

### **1. Use ngrok (Recommended for Testing)**

```bash
# Run this script to expose your local API publicly
./start-with-ngrok.sh
```

This creates a secure tunnel:
```
[PayFast] ✅ → [ngrok.io] ✅ → [Your localhost:3001]
```

### **2. Deploy to AWS (Production)**

```bash
sam deploy --guided
```

### **3. Use Request Bin for Testing**

For testing webhooks without ngrok:
- Go to https://requestbin.com
- Create a temporary URL  
- Use that URL in PayFast sandbox

## 🧪 Testing Flow

### **With ngrok:**
1. Run `./start-with-ngrok.sh`
2. Copy the ngrok URL (e.g., `https://abc123.ngrok.io`)
3. Update `.env` file:
   ```
   PAYFAST_RETURN_URL=https://abc123.ngrok.io/return
   PAYFAST_CANCEL_URL=https://abc123.ngrok.io/cancel
   PAYFAST_NOTIFY_URL=https://abc123.ngrok.io/notify
   ```
4. Test payment: `https://abc123.ngrok.io/pay/1`
5. PayFast will redirect to: `https://abc123.ngrok.io/return?payment_status=COMPLETE`

### **Without ngrok (localhost only):**
- ✅ You can test: `/pay/1`, `/getProducts`  
- ❌ PayFast cannot reach: `/return`, `/cancel`, `/notify`
- ✅ You can manually test: `http://localhost:3001/return?payment_id=123&status=COMPLETE`

## 🔧 Current Configuration

Your `.env` file has:
```
PAYFAST_RETURN_URL=http://localhost:3000/return  ← PayFast cannot reach this
PAYFAST_CANCEL_URL=http://localhost:3000/cancel  ← PayFast cannot reach this  
PAYFAST_NOTIFY_URL=http://localhost:3000/notify  ← PayFast cannot reach this
```

**For PayFast to work**, these need to be **publicly accessible URLs** like:
- `https://yourdomain.com/return`
- `https://abc123.ngrok.io/return`
- `https://your-api-gateway-url/return`
