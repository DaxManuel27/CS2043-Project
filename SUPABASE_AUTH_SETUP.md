# Supabase Authentication Setup Guide

This guide will walk you through setting up Supabase Authentication for your CS2043 Spring Boot application.

---

## Step 1: Get Your Supabase Credentials

1. Go to your Supabase Dashboard: https://supabase.com/dashboard
2. Select your project: `rbsnpfflwdfjwmuiaydf`
3. Navigate to **Settings** > **API**
4. Copy the following values:
   - **Project URL**: `https://rbsnpfflwdfjwmuiaydf.supabase.co`
   - **anon/public key**: This is your API key
   - **JWT Secret**: Click "Reveal" to see it

---

## Step 2: Update application.properties

Edit `/src/main/resources/application.properties` and replace the placeholders:

```properties
# Supabase Auth Configuration
supabase.url=https://rbsnpfflwdfjwmuiaydf.supabase.co
supabase.key=YOUR_ANON_KEY_HERE  # Replace with actual anon key
supabase.jwt.secret=YOUR_JWT_SECRET_HERE  # Replace with actual JWT secret
```

**Example:**
```properties
supabase.url=https://rbsnpfflwdfjwmuiaydf.supabase.co
supabase.key=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJic25wZmZsd2RmandtdWlheWRmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzMzNTE3OTUsImV4cCI6MjA0ODkyNzc5NX0.abc123...
supabase.jwt.secret=your-super-secret-jwt-secret-at-least-256-bits
```

---

## Step 3: Create Users in Supabase

### Option A: Via Supabase Dashboard

1. Go to **Authentication** > **Users**
2. Click **Add User**
3. Fill in:
   - **Email**: `admin@company.com`
   - **Password**: `admin123`
   - **User Metadata** (click "Add metadata"):
     ```json
     {
       "firstName": "Admin",
       "lastName": "User",
       "role": "Admin"
     }
     ```
4. Click **Create User**
5. Check your email and confirm the user (or disable email confirmation in Auth settings)

### Option B: Via SQL (Direct Database)

```sql
-- This is handled by Supabase Auth automatically when you create users via the dashboard
-- Don't run this directly unless you know what you're doing
```

---

## Step 4: Configure Supabase Auth Settings

1. Go to **Authentication** > **Providers**
2. Enable **Email** provider
3. Go to **Authentication** > **URL Configuration**
   - Set **Site URL**: `http://localhost:3000` (or your frontend URL)
   - Set **Redirect URLs**: Add `http://localhost:3000/dashboard.html`

4. Go to **Authentication** > **Policies** (Optional)
   - For development, you can disable email confirmation:
     - Go to **Settings** > **Auth**
     - Uncheck "Enable email confirmations"

---

## Step 5: Test the Setup

### 5.1 Start Your Backend

```bash
./mvnw spring-boot:run
```

The backend should start on `http://localhost:8080`

### 5.2 Test Login via API

Using curl:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@company.com",
    "password": "admin123"
  }'
```

Expected response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "...",
  "user": {
    "id": "uuid-here",
    "email": "admin@company.com",
    "firstName": "Admin",
    "lastName": "User",
    "role": "Admin"
  },
  "expiresIn": 3600
}
```

### 5.3 Test Frontend

1. Open `frontend/index.html` in a browser
2. Login with:
   - **Email**: `admin@company.com`
   - **Password**: `admin123`
3. You should be redirected to the dashboard

---

## Step 6: Create Additional Users

### Create an Employee User

1. Go to Supabase Dashboard > **Authentication** > **Users**
2. Add user:
   - **Email**: `employee@company.com`
   - **Password**: `employee123`
   - **User Metadata**:
     ```json
     {
       "firstName": "John",
       "lastName": "Employee",
       "role": "Employee"
     }
     ```

---

## Architecture Overview

```
┌─────────────┐         ┌──────────────────┐         ┌────────────────┐
│   Frontend  │  HTTP   │  Spring Boot     │  HTTP   │   Supabase     │
│   (HTML/JS) ├────────►│  Backend         ├────────►│   Auth API     │
│             │         │                  │         │                │
└─────────────┘         └──────────────────┘         └────────────────┘
      │                          │                            │
      │                          │                            │
      └──────── JWT Token ───────┘                            │
                                                              │
                                        ┌─────────────────────┘
                                        │
                                        ▼
                              ┌──────────────────┐
                              │   PostgreSQL     │
                              │   Database       │
                              └──────────────────┘
```

### Flow:

1. User enters credentials in frontend
2. Frontend sends to `/api/auth/login` on Spring Boot
3. Spring Boot forwards to Supabase Auth API
4. Supabase validates credentials and returns JWT token
5. Spring Boot returns token to frontend
6. Frontend stores token in localStorage
7. All subsequent API calls include: `Authorization: Bearer <token>`
8. Spring Boot validates token on every request using JwtAuthenticationFilter

---

## Security Features

✅ **JWT Token Validation** - Server-side validation on every request  
✅ **Role-Based Access Control** - Admin vs Employee permissions  
✅ **Secure Password Storage** - Bcrypt hashing via Supabase  
✅ **Token Expiration** - Automatic token refresh  
✅ **CORS Protection** - Configured in SecurityConfig  
✅ **HTTPS Support** - Production-ready with SSL  

---

## Troubleshooting

### Error: "Invalid credentials"
- Check that the user exists in Supabase Dashboard > Authentication > Users
- Verify email and password are correct
- Check if email confirmation is required (and user has confirmed)

### Error: "Invalid token"
- Check that `supabase.jwt.secret` matches your Supabase JWT Secret
- Verify the token hasn't expired
- Try logging out and logging back in

### Error: "Connection refused"
- Ensure Spring Boot backend is running on port 8080
- Check if PostgreSQL database is accessible
- Verify Supabase project is not paused

### Error: "CORS policy"
- Check SecurityConfig CORS configuration
- Verify frontend is accessing the correct backend URL

---

## API Endpoints

### Public Endpoints (No Authentication Required)

- `POST /api/auth/login` - Login with email/password
- `POST /api/auth/logout` - Logout current user

### Protected Endpoints (Requires JWT Token)

- `GET /api/auth/me` - Get current user info
- `GET /employees` - Get all employees
- `POST /employees` - Create employee
- `PUT /employees/{id}` - Update employee
- `DELETE /employees/{id}` - Delete employee
- `GET /leave-requests` - Get all leave requests
- `POST /leave-requests` - Create leave request
- `POST /leave-requests/{id}/approve` - Approve leave request
- `POST /leave-requests/{id}/reject` - Reject leave request
- `DELETE /leave-requests/{id}` - Delete leave request

---

## Next Steps

1. ✅ Test login with admin user
2. ✅ Test employee CRUD operations
3. ✅ Test leave request workflow
4. 🔲 Add password reset functionality
5. 🔲 Add user registration page
6. 🔲 Add email verification
7. 🔲 Add OAuth providers (Google, GitHub)
8. 🔲 Add audit logging
9. 🔲 Add rate limiting
10. 🔲 Deploy to production

---

## Production Checklist

Before deploying to production:

- [ ] Change all default passwords
- [ ] Enable email confirmation
- [ ] Set up proper CORS origins (not "*")
- [ ] Enable HTTPS/SSL
- [ ] Set up environment variables (don't commit secrets!)
- [ ] Enable Supabase Row Level Security (RLS)
- [ ] Set up database backups
- [ ] Configure rate limiting
- [ ] Set up monitoring and logging
- [ ] Review security settings

---

## Support

For issues or questions:
- Check the Supabase documentation: https://supabase.com/docs/guides/auth
- Spring Security docs: https://docs.spring.io/spring-security/reference/
- JWT.io for debugging tokens: https://jwt.io/

