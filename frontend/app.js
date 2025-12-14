// --- Configuration ---
/**
 * Base URL for the backend API server.
 */
const BASE_URL = 'http://localhost:8080';

// --- Admin Credentials (Local Authentication) ---
const ADMIN_CREDENTIALS = {
    username: 'admin@company.com',
    password: 'admin123'
};

// --- Local Data Management (Simulation for Write Operations) ---

/**
 * Ensures initial dummy data exists in local storage if not present.
 */
function initializeLocalData() {
    if (!localStorage.getItem('employees')) {
        const initialEmployees = [
            { id: 'emp-001', firstName: 'Alice', lastName: 'Johnson', email: 'alice@company.com', password: 'admin', role: 'Employee', salary: 60000, missedDays: 5 },
            { id: 'emp-002', firstName: 'Bob', lastName: 'Smith', email: 'bob@company.com', password: 'password', role: 'Employee', salary: 75000, missedDays: 2 }
        ];
        localStorage.setItem('employees', JSON.stringify(initialEmployees));
    }
    if (!localStorage.getItem('leaveRequests')) {
        const initialRequests = [
            { id: 101, employeeId: 'emp-001', employeeName: 'Alice Johnson', type: 'Vacation', startDate: '2025-10-01', endDate: '2025-10-05', reason: 'Annual Leave', status: 'Pending' }
        ];
        localStorage.setItem('leaveRequests', JSON.stringify(initialRequests));
    }
}
initializeLocalData(); // Run on script load

function getEmployeesLocal() {
    return JSON.parse(localStorage.getItem('employees')) || [];
}

function setEmployeesLocal(employees) {
    localStorage.setItem('employees', JSON.stringify(employees));
}

function getLeaveRequestsLocal() {
    return JSON.parse(localStorage.getItem('leaveRequests')) || [];
}

function setLeaveRequestsLocal(leaves) {
    localStorage.setItem('leaveRequests', JSON.stringify(leaves));
}

// --- Utility Functions ---

function getCurrentUser() {
    return JSON.parse(localStorage.getItem("currentUser"));
}

function setCurrentUser(user) {
    localStorage.setItem("currentUser", JSON.stringify(user));
}

// --- Auth Token Helper Functions ---

function getAuthToken() {
    return localStorage.getItem('authToken');
}

function setAuthToken(token) {
    localStorage.setItem('authToken', token);
}

function clearAuthToken() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
    localStorage.removeItem('isAdminSession');
}

async function checkAuth(requiredRole) {
    const user = getCurrentUser();
    const token = getAuthToken();
    const isAdminSession = localStorage.getItem('isAdminSession');
    
    // Check if user is logged in (either via Supabase token or admin session)
    if (!user || (!token && !isAdminSession)) {
        window.location.href = "index.html";
        return false;
    }
    if (requiredRole && user.role !== requiredRole) {
        alert("You don't have permission to access this page.");
        window.location.href = "dashboard.html";
        return false;
    }
    return true;
}

function initApp() {
    // Check if user is already logged in
    const user = getCurrentUser();
    const token = getAuthToken();
    const isAdmin = localStorage.getItem('isAdminSession');
    
    if (user && (token || isAdmin)) {
        // Already logged in, redirect to dashboard
        console.log("User already logged in:", user.email || user.username);
        window.location.href = "dashboard.html";
    }
}

// --- Login Form Toggle Functions ---

function showEmployeeLogin() {
    document.getElementById('employeeLoginForm').style.display = 'block';
    document.getElementById('adminLoginForm').style.display = 'none';
    document.getElementById('employeeToggle').classList.add('active');
    document.getElementById('adminToggle').classList.remove('active');
}

function showAdminLogin() {
    document.getElementById('employeeLoginForm').style.display = 'none';
    document.getElementById('adminLoginForm').style.display = 'block';
    document.getElementById('employeeToggle').classList.remove('active');
    document.getElementById('adminToggle').classList.add('active');
}

/**
 * Handles Employee Login via Supabase Auth
 * Always logs in as Employee role
 */
async function handleEmployeeLogin(event) {
    event.preventDefault();
    const email = document.getElementById("employeeEmail").value.trim();
    const password = document.getElementById("employeePassword").value.trim();
    const errorMsg = document.getElementById("employeeLoginError");
    
    errorMsg.textContent = "Logging in...";

    try {
        const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, password })
        });

        if (response.ok) {
            const result = await response.json();
            
            // Store JWT token
            setAuthToken(result.token);
            
            // Set role as Employee for Supabase login
            const employeeUser = {
                ...result.user,
                role: 'Employee'
            };
            localStorage.setItem("currentUser", JSON.stringify(employeeUser));
            localStorage.removeItem('isAdminSession');
            
            errorMsg.textContent = "";
            window.location.href = "dashboard.html";
        } else {
            const error = await response.json();
            errorMsg.textContent = error.error || "Login failed. Please check your credentials.";
        }
    } catch (error) {
        console.error("Login error:", error);
        errorMsg.textContent = "Login failed: " + error.message;
    }
}

/**
 * Handles Admin Login via Local Authentication
 * Authenticates against hardcoded admin credentials
 */
async function handleAdminLogin(event) {
    event.preventDefault();
    const username = document.getElementById("adminUsername").value.trim();
    const password = document.getElementById("adminPassword").value.trim();
    const errorMsg = document.getElementById("adminLoginError");
    
    errorMsg.textContent = "";

    // Validate against local admin credentials
    if (username === ADMIN_CREDENTIALS.username && password === ADMIN_CREDENTIALS.password) {
        // Create admin user session
        const adminUser = {
            id: 'admin-001',
            username: username,
            email: 'admin@company.com',
            firstName: 'System',
            lastName: 'Administrator',
            role: 'Admin'
        };
        
        localStorage.setItem("currentUser", JSON.stringify(adminUser));
        localStorage.setItem('isAdminSession', 'true');
        // No JWT token needed for local admin auth
        localStorage.removeItem('authToken');
        
        window.location.href = "dashboard.html";
    } else {
        errorMsg.textContent = "Invalid admin credentials. Please try again.";
    }
}

// Legacy handleLogin function for backwards compatibility
async function handleLogin(event) {
    return handleEmployeeLogin(event);
}

// --- API Access Functions (READ Operations - Uses Backend) ---

/**
 * Generic function to fetch data from the backend API.
 */
const API_BASE_URL = "http://localhost:8080";

async function fetchData(endpoint) {
    const token = getAuthToken();
    const isAdminSession = localStorage.getItem('isAdminSession');
    const headers = {
        'Content-Type': 'application/json'
    };
    
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            headers: headers
        });

        if (response.status === 401) {
            // Token expired or invalid - only redirect if not an admin session
            if (!isAdminSession) {
                clearAuthToken();
                window.location.href = "index.html";
                return [];
            }
            // For admin sessions, the backend may not require auth for some endpoints
            // Continue without redirecting
        }

        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error(`Failed to load data from ${endpoint}:`, error);
        return [];
    }
}


async function fetchAllEmployees() {
    // Corresponds to the backend GET /dashboard/employees
    return await fetchData('/employees');
}

async function fetchAllLeaveRequests() {
    // Corresponds to the backend GET /leave-requests
    return await fetchData('/leave-requests');
}

// --- Authentication Functions ---

/**
 * Handles logout for both Employee (Supabase) and Admin (local) sessions
 */
async function handleLogout() {
    const token = getAuthToken();
    const isAdminSession = localStorage.getItem('isAdminSession');
    
    // Only call Supabase logout if it was an employee session with a token
    if (token && !isAdminSession) {
        try {
            await fetch(`${API_BASE_URL}/api/auth/logout`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
        } catch (error) {
            console.error("Logout error:", error);
        }
    }
    
    clearAuthToken();
    window.location.href = "index.html";
}

// --- Dashboard Logic ---

async function loadDashboard() {
    const user = getCurrentUser();
    const token = getAuthToken();
    const isAdminSession = localStorage.getItem('isAdminSession');
    
    // Check if user is logged in (either via Supabase token or admin session)
    if (!user || (!token && !isAdminSession)) {
        window.location.href = "index.html";
        return;
    }

    // Update dashboard title based on role
    document.getElementById("dashboardTitle").textContent = user.role === 'Admin' ? 'Admin Dashboard' : 'Employee Portal';

    if (user.role === 'Admin') {
        document.getElementById("adminView").style.display = 'block';
        document.getElementById("employeeView").style.display = 'none';

        const employees = await fetchAllEmployees();
        const leaves = await fetchAllLeaveRequests();

        displayEmployees(employees);
        displayLeaveRequestsAdmin(leaves);

    } else {
        document.getElementById("adminView").style.display = 'none';
        document.getElementById("employeeView").style.display = 'block';

        const leaves = await fetchAllLeaveRequests();

        displayMyLeaveHistory(user.id, leaves);
    }
}

// Admin Views

function displayEmployees(employees) {
    const tableBody = document.getElementById("employeeTableBody");
    if (!tableBody) return;

    tableBody.innerHTML = "";

    employees.forEach((emp, index) => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${index + 1}</td>
            <td>${emp.firstName} ${emp.lastName}</td>
            <td>$${(emp.salary || 0).toLocaleString()}</td>
            <td>${emp.missedDays || 0}</td>
            <td>
                <button class="button-edit" onclick="editEmployee('${emp.employeeID}')">Edit</button>
            </td>
        `;
        tableBody.appendChild(row);
    });
}

function displayLeaveRequestsAdmin(allLeaves) {
    const tableBody = document.getElementById("leaveTableBody");
    if (!tableBody) return;

    // Filter for pending leaves only
    // A leave is pending if: status is PENDING AND approved is false
    // This handles both new records (with status) and legacy records
    const pendingLeaves = allLeaves.filter(l => {
        // If status is explicitly APPROVED or REJECTED, it's not pending
        if (l.status === 'APPROVED' || l.status === 'REJECTED') {
            return false;
        }
        // If approved is true (legacy data), it's not pending
        if (l.approved) {
            return false;
        }
        // Otherwise it's pending
        return true;
    });
    tableBody.innerHTML = "";

    if (pendingLeaves.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="5" style="text-align: center;">No pending leave requests.</td></tr>';
        return;
    }

    pendingLeaves.forEach((leave) => {
        const employeeName = leave.employeeName || 'Unknown';
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>#${leave.requestID}</td>
            <td>${employeeName}</td>
            <td>${leave.startDate} to ${leave.endDate}</td>
            <td>${leave.totalDays} days</td>
            <td>
                <button class="button-approve" onclick="updateLeaveStatus('${leave.requestID}', 'Approved')">Approve</button>
                <button class="button-reject" onclick="updateLeaveStatus('${leave.requestID}', 'Rejected')">Reject</button>
            </td>
        `;
        tableBody.appendChild(row);
    });
}

/**
 * Updates leave request status (approve or reject).
 */
async function updateLeaveStatus(leaveId, status) {
    const token = getAuthToken();
    try {
        let response;
        
        if (status === 'Approved') {
            // Approve the leave request
            response = await fetch(`${API_BASE_URL}/leave-requests/${leaveId}/approve`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                }
            });
        } else {
            // Reject the leave request (keeps it in DB with REJECTED status)
            response = await fetch(`${API_BASE_URL}/leave-requests/${leaveId}/reject`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                }
            });
        }

        if (!response.ok) {
            throw new Error(`Failed to ${status.toLowerCase()} leave request: ${response.status}`);
        }

        const message = status === 'Approved' 
            ? 'Leave request approved' 
            : 'Leave request rejected';
        alert(message);
        
        // Refresh the dashboard to show the change
        loadDashboard();
    } catch (error) {
        console.error('Error updating leave status:', error);
        alert(`Error: ${error.message}`);
    }
}

// Employee Views

function displayMyLeaveHistory(employeeId, allLeaves) {
    const tableBody = document.getElementById("myLeaveTableBody");
    if (!tableBody) return;

    // Note: Backend doesn't track employeeId on LeaveRequest yet, showing all leaves
    tableBody.innerHTML = "";

    if (allLeaves.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="4" style="text-align: center;">No leave requests found.</td></tr>';
        return;
    }

    allLeaves.forEach((leave) => {
        // Determine the display status
        // Priority: Use status field if it's APPROVED or REJECTED, otherwise check approved flag
        let status;
        if (leave.status === 'APPROVED' || leave.status === 'REJECTED') {
            // Use the explicit status
            status = leave.status.charAt(0).toUpperCase() + leave.status.slice(1).toLowerCase();
        } else if (leave.approved) {
            // Legacy: approved = true means approved
            status = 'Approved';
        } else {
            // Not approved and status is PENDING or null
            status = leave.status === 'REJECTED' ? 'Rejected' : 'Pending';
        }
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${leave.startDate} to ${leave.endDate}</td>
            <td>${leave.totalDays} days</td>
            <td><span class="status-badge status-${status}">${status}</span></td>
        `;
        tableBody.appendChild(row);
    });
}

// Leave Request Form

/**
 * Submits a new leave request to the backend.
 */
async function submitLeaveRequest(event) {
    event.preventDefault();
    const form = event.target;
    const token = getAuthToken();
    const user = getCurrentUser();

    const leaveRequestData = {
        startDate: form.startDate.value,
        endDate: form.endDate.value
    };

    try {
        const response = await fetch(`${API_BASE_URL}/leave-requests`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(leaveRequestData)
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.error || `Failed to submit leave request: ${response.status}`);
        }

        alert('Leave request submitted successfully!');
        window.location.href = 'dashboard.html';
    } catch (error) {
        console.error('Error submitting leave request:', error);
        alert(`Error: ${error.message}`);
    }
}

// Employee Form (Add/Edit)

function editEmployee(id) {
    localStorage.setItem("editEmployeeId", id);
    window.location.href = "employee-form.html";
}

async function loadEmployeeForm() {
    await checkAuth('Admin');
    const id = localStorage.getItem("editEmployeeId");
    const form = document.getElementById("employeeForm");
    const authFields = document.getElementById("authFields");

    if (!id) {
        // New employee - show auth fields
        document.querySelector('h1').textContent = "Add New Employee";
        form.querySelector('button[type="submit"]').textContent = "Add Employee";
        if (authFields) {
            authFields.style.display = 'block';
            form.email.required = true;
            form.password.required = true;
        }
        return;
    }

    // Editing existing employee - hide auth fields
    if (authFields) {
        authFields.style.display = 'none';
        form.email.required = false;
        form.password.required = false;
    }

    // Fetch data for pre-filling the form (Uses API, or local data as fallback)
    const employees = await fetchAllEmployees();
    const emp = employees.find(e => e.employeeID == id);

    if (emp) {
        document.querySelector('h1').textContent = "Edit Employee";
        form.firstName.value = emp.firstName;
        form.lastName.value = emp.lastName;
        form.salary.value = emp.salary;
        form.missedDays.value = emp.missedDays || 0;
        form.querySelector('button[type="submit"]').textContent = "Save Changes";
    } else {
        localStorage.removeItem("editEmployeeId");
    }
}

/**
 * Uses local storage to save or update employee data (POST/PUT simulation).
 */
async function saveEmployee(event) {
    event.preventDefault();
    const form = event.target;
    const isEdit = localStorage.getItem("editEmployeeId");
    const token = getAuthToken();
    
    const employeeData = {
        firstName: form.firstName.value,
        lastName: form.lastName.value,
        salary: parseFloat(form.salary.value),
        missedDays: parseInt(form.missedDays.value) || 0
    };

    try {
        let response;
        if (isEdit) {
            // Update existing employee - PUT request
            response = await fetch(`${API_BASE_URL}/employees/${isEdit}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(employeeData)
            });
            
            if (!response.ok) {
                throw new Error(`Failed to update employee: ${response.status}`);
            }
            
            alert("Employee updated successfully!");
        } else {
            // New employee - first create auth user in Supabase
            const email = form.email.value;
            const password = form.password.value;
            
            // Step 1: Create auth user in Supabase
            const signupResponse = await fetch(`${API_BASE_URL}/api/auth/signup`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    email: email,
                    password: password,
                    firstName: employeeData.firstName,
                    lastName: employeeData.lastName
                })
            });
            
            if (!signupResponse.ok) {
                const errorData = await signupResponse.json().catch(() => ({}));
                throw new Error(errorData.error || `Failed to create auth account: ${signupResponse.status}`);
            }
            
            // Step 2: Create employee record in database
            response = await fetch(`${API_BASE_URL}/employees`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(employeeData)
            });
            
            if (!response.ok) {
                throw new Error(`Failed to create employee: ${response.status}`);
            }
            
            alert("Employee added successfully! They can now log in with their email and password.");
        }

        localStorage.removeItem("editEmployeeId");
        window.location.href = "dashboard.html";
        
    } catch (error) {
        console.error('Error saving employee:', error);
        alert(`Error: ${error.message}`);
    }
}

// Report Viewer (Uses only fetched data, already functional)

async function generateReport(type) {
    await checkAuth('Admin');

    const employees = await fetchAllEmployees();
    const leaves = await fetchAllLeaveRequests();

    let csvContent = "data:text/csv;charset=utf-8,";
    let fileName = "";
    let data = [];

    switch (type) {
        case "EmployeeList":
            fileName = "Employee_List.csv";
            data = employees.map(e => ({
                ID: e.id,
                Name: `${e.firstName} ${e.lastName}`,
                Email: e.email,
                Role: e.role || "Employee",
                Salary: e.salary,
                MissedDays: e.missedDays || 0
            }));
            break;
        case "LeaveSummary":
            fileName = "All_Leave_Summary.csv";
            data = leaves.map(l => ({
                ID: l.id,
                Employee: l.employeeName || (l.employee ? l.employee.firstName + ' ' + l.employee.lastName : 'N/A'),
                Type: l.type,
                StartDate: l.startDate,
                EndDate: l.endDate,
                Reason: l.reason.replace(/,/g, ';'),
                Status: l.status
            }));
            break;
        case "PendingLeaves":
            fileName = "Pending_Leave_Summary.csv";
            data = leaves.filter(l => l.status === 'Pending').map(l => ({
                ID: l.id,
                Employee: l.employeeName || (l.employee ? l.employee.firstName + ' ' + l.employee.lastName : 'N/A'),
                Type: l.type,
                StartDate: l.startDate,
                EndDate: l.endDate,
                Reason: l.reason.replace(/,/g, ';'),
                Status: l.status
            }));
            break;
    }

    if (data.length === 0) {
        alert("No data available for this report.");
        return;
    }

    const headers = Object.keys(data[0]);
    csvContent += headers.join(',') + "\n";
    data.forEach(row => {
        csvContent += headers.map(header => row[header]).join(',') + "\n";
    });

    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", fileName);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}