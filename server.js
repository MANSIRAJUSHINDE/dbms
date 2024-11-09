const express = require('express');
const mysql = require('mysql2');
const bodyParser = require('body-parser');
const cors = require('cors');

// Create an express app
const app = express();
const port = 3000;

// Middleware
app.use(cors());  // Enable CORS
app.use(bodyParser.json());  // Parse incoming JSON data

// MySQL database connection
const db = mysql.createConnection({
  host: 'localhost',
  user: 'root',
  password: 'root',  // Use your MySQL password here if needed
  database: 'crud_db'  // Use your database name
});

// Connect to MySQL
db.connect((err) => {
  if (err) {
    console.error('DB connection failed:', err.stack);
    return;
  }
  console.log('Connected to MySQL');
});

// API Routes
// Get all users
app.get('/api/users', (req, res) => {
  db.query('SELECT * FROM users', (err, results) => {
    if (err) return res.status(500).json({ success: false, message: err.message });
    res.json(results);
  });
});

// Get a single user by ID for editing
app.get('/api/users/:id', (req, res) => {
  const userId = req.params.id;
  db.query('SELECT * FROM users WHERE id = ?', [userId], (err, results) => {
    if (err) return res.status(500).json({ success: false, message: err.message });
    if (results.length === 0) {
      return res.status(404).json({ success: false, message: 'User not found.' });
    }
    res.json(results[0]);  // Send back the first user
  });
});

// Add a new user
app.post('/api/users', (req, res) => {
  const { name, email } = req.body;
  db.query('INSERT INTO users (name, email) VALUES (?, ?)', [name, email], (err, result) => {
    if (err) return res.status(500).json({ success: false, message: err.message });
    res.json({ success: true, id: result.insertId });
  });
});

// Update an existing user
app.put('/api/users', (req, res) => {
  const { id, name, email } = req.body;

  if (!id || !name || !email) {
    return res.status(400).json({ success: false, message: 'ID, name, and email are required.' });
  }

  db.query('UPDATE users SET name = ?, email = ? WHERE id = ?', [name, email, id], (err, result) => {
    if (err) return res.status(500).json({ success: false, message: err.message });
    res.json({ success: true });
  });
});

// Delete a user
app.delete('/api/users', (req, res) => {
  const { id } = req.body;
  db.query('DELETE FROM users WHERE id = ?', [id], (err, result) => {
    if (err) return res.status(500).json({ success: false, message: err.message });
    res.json({ success: true });
  });
});

// Start the server
app.listen(port, () => {
  console.log(`Server running at http://localhost:${port}`);
});
