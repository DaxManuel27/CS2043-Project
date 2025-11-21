// --- Configuration ---
/**
 * Base URL for the backend API server.
 */
const BASE_URL = 'http://localhost:8080';

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

/**
 * Checks if a user is authenticated and has the required role.
 */
function checkAuth(role) {
    const user = getCurrentUser();
    if (!user) {
        window.location.href = "index.html";
        return;
    }
    if (role === 'Admin' && user.role !== 'Admin') {
        alert("Access denied.");
        window.location.href = "dashboard.html";
        return;
    }
}

// --- API Access Functions (READ Operations - Uses Backend) ---

/**
 * Generic function to fetch data from the backend API.
 */
async function fetchData(endpoint) {
    try {
        const response = await fetch(`${BASE_URL}${endpoint}`);
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        return await response.json();
    } catch (error) {
        // Fallback or error message for failed backend connection
        console.error(`Failed to load data from ${endpoint}:`, error);
        alert("Failed to load data from server. Please ensure the backend is running. Using local data simulation.");
        // IMPORTANT: If API fails, return the local data simulation for testing continuity.
        if (endpoint.includes('employees')) return getEmployeesLocal();
        if (endpoint.includes('leave-requests')) return getLeaveRequestsLocal();
        return [];
    }
}

async function fetchAllEmployees() {
    // Corresponds to the backend GET /dashboard/employees
    return await fetchData('/dashboard/employees');
}

async function fetchAllLeaveRequests() {
    // Corresponds to the backend GET /dashboard/leave-requests
    return await fetchData('/dashboard/leave-requests');
}

// --- Authentication Functions ---

/**
 * Performs login validation using fetched employee data.
 */
async function apiLogin(username, password) {
    // Admin credentials check
    if (username === "admin@company.com" && password === "admin") {
        return { success: true, user: { id: 'admin-001', firstName: "System", lastName: "Admin", email: "admin@company.com", role: "Admin" } };
    }

    // Fetch all employees to validate standard employee login (Uses API, or local data as fallback)
    const employees = await fetchAllEmployees();
    const user = employees.find(emp => emp.email === username && emp.password === password);

    if (user) {
        return { success: true, user: { ...user, role: "Employee" } };
    } else {
        return { success: false, message: "Invalid email or password." };
    }
}

async function handleLogin(event) {
    event.preventDefault();
    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value.trim();
    const errorMsg = document.getElementById("loginError");
    errorMsg.textContent = "Logging in...";

    const result = await apiLogin(username, password);

    if (result.success) {
        setCurrentUser(result.user);
        errorMsg.textContent = "";
        window.location.href = "dashboard.html";
    } else {
        errorMsg.textContent = result.message;
    }
}

function handleLogout() {
    localStorage.removeItem("currentUser");
    window.location.href = "index.html";
}

// --- Dashboard Logic ---

async function loadDashboard() {
    const user = getCurrentUser();
    if (!user) {
        window.location.href = "index.html";
        return;
    }

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

    const displayList = employees.filter(e => e.role !== 'Admin');
    tableBody.innerHTML = "";

    displayList.forEach((emp, index) => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${index + 1}</td>
            <td>${emp.firstName} ${emp.lastName}</td>
            <td>${emp.email}</td>
            <td>$${(emp.salary || 0).toLocaleString()}</td>
            <td>
                <button class="button-edit" onclick="editEmployee('${emp.id}')">Edit</button>
            </td>
        `;
        tableBody.appendChild(row);
    });
}

function displayLeaveRequestsAdmin(allLeaves) {
    const tableBody = document.getElementById("leaveTableBody");
    if (!tableBody) return;

    const pendingLeaves = allLeaves.filter(l => l.status === 'Pending');
    tableBody.innerHTML = "";

    if (pendingLeaves.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="5" style="text-align: center;">No pending leave requests.</td></tr>';
        return;
    }

    pendingLeaves.forEach((leave) => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${leave.employeeName || (leave.employee ? leave.employee.firstName + ' ' + leave.employee.lastName : 'N/A')}</td>
            <td>${leave.type}</td>
            <td>${leave.startDate} to ${leave.endDate}</td>
            <td>${leave.reason}</td>
            <td>
                <button class="button-approve" onclick="updateLeaveStatus('${leave.id}', 'Approved')">Approve</button>
                <button class="button-reject" onclick="updateLeaveStatus('${leave.id}', 'Rejected')">Reject</button>
            </td>
        `;
        tableBody.appendChild(row);
    });
}

/**
 * Uses local storage to update leave status (POST/PUT simulation).
 */
async function updateLeaveStatus(leaveId, status) {
    let leaves = getLeaveRequestsLocal();
    const index = leaves.findIndex(l => l.id == leaveId);

    if (index !== -1) {
        leaves[index].status = status;
        setLeaveRequestsLocal(leaves);
        alert(`Leave request ${leaveId} status updated to ${status}.`);
    } else {
        alert(`Leave request ${leaveId} not found locally.`);
    }

    // Refresh the dashboard to show the change
    loadDashboard();
}

// Employee Views

function displayMyLeaveHistory(employeeId, allLeaves) {
    const tableBody = document.getElementById("myLeaveTableBody");
    if (!tableBody) return;

    const myLeaves = allLeaves.filter(l => l.employeeId == employeeId);
    tableBody.innerHTML = "";

    if (myLeaves.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="4" style="text-align: center;">You have no submitted leave requests.</td></tr>';
        return;
    }

    myLeaves.forEach((leave) => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${leave.type}</td>
            <td>${leave.startDate}</td>
            <td>${leave.endDate}</td>
            <td><span class="status-badge status-${leave.status}">${leave.status}</span></td>
        `;
        tableBody.appendChild(row);
    });
}

// Leave Request Form

/**
 * Uses local storage to submit a new leave request (POST simulation).
 */
async function submitLeaveRequest(event) {
    event.preventDefault();
    const user = getCurrentUser();
    const form = event.target;

    const newRequest = {
        id: Date.now(), // Simple unique ID
        employeeId: user.id,
        employeeName: `${user.firstName} ${user.lastName}`,
        type: form.leaveType.value,
        startDate: form.startDate.value,
        endDate: form.endDate.value,
        reason: form.reason.value,
        status: 'Pending'
    };

    let leaves = getLeaveRequestsLocal();
    leaves.push(newRequest);
    setLeaveRequestsLocal(leaves);

    alert("Leave request submitted successfully!");
    window.location.href = "dashboard.html";
}

// Employee Form (Add/Edit)

function editEmployee(id) {
    localStorage.setItem("editEmployeeId", id);
    window.location.href = "employee-form.html";
}

async function loadEmployeeForm() {
    checkAuth('Admin');
    const id = localStorage.getItem("editEmployeeId");
    const form = document.getElementById("employeeForm");

    if (!id) {
        document.querySelector('h1').textContent = "Add New Employee";
        form.querySelector('button[type="submit"]').textContent = "Add Employee";
        return;
    }

    // Fetch data for pre-filling the form (Uses API, or local data as fallback)
    const employees = await fetchAllEmployees();
    const emp = employees.find(e => e.id == id);

    if (emp) {
        document.querySelector('h1').textContent = "Edit Employee";
        form.firstName.value = emp.firstName;
        form.lastName.value = emp.lastName;
        form.email.value = emp.email;
        form.password.value = emp.password;
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
    
    let employees = getEmployeesLocal();
    const newEmployee = {
        firstName: form.firstName.value,
        lastName: form.lastName.value,
        email: form.email.value,
        password: form.password.value,
        salary: parseFloat(form.salary.value),
        missedDays: parseInt(form.missedDays.value) || 0,
        role: 'Employee'
    };

    if (isEdit) {
        // Update existing
        const index = employees.findIndex(e => e.id == isEdit);
        if (index !== -1) {
            employees[index] = { ...employees[index], ...newEmployee };
            alert("Employee data updated successfully locally.");
        }
    } else {
        // Add new
        newEmployee.id = `emp-${Date.now()}`;
        employees.push(newEmployee);
        alert("New employee added successfully locally.");
    }

    setEmployeesLocal(employees);
    localStorage.removeItem("editEmployeeId");
    window.location.href = "dashboard.html";
}

// Report Viewer (Uses only fetched data, already functional)

async function generateReport(type) {
    checkAuth('Admin');

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
