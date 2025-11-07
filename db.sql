-- Create Employee table
CREATE TABLE Employee (
  EmployeeID uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  FirstName text NOT NULL,
  LastName text NOT NULL,
  Salary numeric(10,2) CHECK (Salary >= 0),
  DOB date NOT NULL,
  RoleType text CHECK (RoleType IN ('Employee', 'Admin'))
);

-- Create LeaveRequest table
CREATE TABLE LeaveRequest (
  LeaveID uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  EmployeeID uuid REFERENCES Employee(EmployeeID) ON DELETE CASCADE,
  StartDate date NOT NULL,
  EndDate date NOT NULL,
  Approved boolean DEFAULT false,
  Type text CHECK (Type IN ('PTO', 'Unpaid'))
);

