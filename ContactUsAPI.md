# Contact Us API

## Overview
A simple endpoint that allows users to send contact messages directly to your admin team via email. The message is forwarded to the configured admin email address (admin@zennest.africa).

## Endpoint

```
POST /api/contact
```

## Authentication
- Requires the `X-API-KEY` header with your service API key
- No user authentication required (public contact form)

## Request

### Headers
- `Content-Type: application/json`
- `X-API-KEY: YOUR_API_KEY`

### Body
```json
{
  "name": "John Doe",
  "email": "user@example.com", 
  "subject": "General Inquiry",
  "message": "This is my message content."
}
```

### Required Fields
- `name` (string) - User's full name
- `email` (string) - User's email address  
- `subject` (string) - Message subject/topic
- `message` (string) - The actual message content

## Response

### Success (200 OK)
```
"Your message has been sent successfully."
```

### Error (500 Internal Server Error)  
```
"Failed to send your message. Please try again later."
```

## Integration Example

### Frontend JavaScript
```javascript
const sendContactMessage = async (contactData) => {
  try {
    const response = await fetch('http://localhost:7500/api/contact', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-API-KEY': 'YOUR_API_KEY'
      },
      body: JSON.stringify(contactData)
    });

    const result = await response.text();
    
    if (response.ok) {
      alert('Message sent successfully!');
    } else {
      alert('Failed to send message. Please try again.');
    }
  } catch (error) {
    console.error('Error:', error);
    alert('Network error. Please try again.');
  }
};

// Usage
const contactForm = {
  name: 'John Doe',
  email: 'john@example.com',
  subject: 'Website Feedback',
  message: 'Great website! I have some suggestions...'
};

sendContactMessage(contactForm);
```

### React Example
```jsx
const ContactForm = () => {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    subject: '',
    message: ''
  });
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    const response = await fetch('/api/contact', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-API-KEY': process.env.REACT_APP_API_KEY
      },
      body: JSON.stringify(formData)
    });
    
    if (response.ok) {
      alert('Message sent!');
      setFormData({ name: '', email: '', subject: '', message: '' });
    }
  };
  
  return (
    <form onSubmit={handleSubmit}>
      <input 
        type="text" 
        placeholder="Name" 
        value={formData.name}
        onChange={(e) => setFormData({...formData, name: e.target.value})}
        required 
      />
      <input 
        type="email" 
        placeholder="Email" 
        value={formData.email}
        onChange={(e) => setFormData({...formData, email: e.target.value})}
        required 
      />
      <input 
        type="text" 
        placeholder="Subject" 
        value={formData.subject}
        onChange={(e) => setFormData({...formData, subject: e.target.value})}
        required 
      />
      <textarea 
        placeholder="Message" 
        value={formData.message}
        onChange={(e) => setFormData({...formData, message: e.target.value})}
        required
      ></textarea>
      <button type="submit">Send Message</button>
    </form>
  );
};
```

## Configuration
The admin email recipient can be configured via:
- Application property: `contact.recipient.email`  
- Default: `admin@zennest.africa`

## Testing
```bash
curl -X POST "http://localhost:7500/api/contact" \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: YOUR_API_KEY" \
  -d '{
    "name": "Test User",
    "email": "test@example.com", 
    "subject": "Test Contact",
    "message": "This is a test message."
  }'
```

---

**Note:** Messages are sent via your email service to the configured admin address. Ensure your email service is properly configured and running.
