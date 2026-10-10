const menuToggle = document.querySelector('.menu-toggle');
const navigation = document.querySelector('#navigation');
function closeMenu() {
  menuToggle.setAttribute('aria-expanded', 'false');
  navigation.classList.remove('is-open');
}
menuToggle.addEventListener('click', () => {
  const open = menuToggle.getAttribute('aria-expanded') !== 'true';
  menuToggle.setAttribute('aria-expanded', String(open));
  navigation.classList.toggle('is-open', open);
});
navigation.querySelectorAll('a').forEach(link => link.addEventListener('click', closeMenu));
document.addEventListener('keydown', event => {
  if (event.key === 'Escape' && menuToggle.getAttribute('aria-expanded') === 'true') {
    closeMenu();
    menuToggle.focus();
  }
});
if ('IntersectionObserver' in window && !window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
  const observer = new IntersectionObserver(entries => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('visible');
        observer.unobserve(entry.target);
      }
    });
  }, { threshold: 0.08 });
  document.querySelectorAll('.reveal').forEach(element => observer.observe(element));
  document.body.classList.add('motion-ready');
}
document.querySelector('#year').textContent = new Date().getFullYear();
const form = document.querySelector('#contact-form');
const message = document.querySelector('#message');
document.querySelectorAll('[data-care]').forEach(link => {
  link.addEventListener('click', () => {
    if (!message.value.trim()) message.value = `I’d like to learn more about ${link.dataset.care}. `;
  });
});
form.addEventListener('submit', async event => {
  event.preventDefault();
  const button = form.querySelector('button[type="submit"]');
  if (button.disabled) return;
  const status = document.querySelector('#form-status');
  const payload = Object.fromEntries(new FormData(form));
  Object.keys(payload).forEach(key => { payload[key] = payload[key].trim(); });
  if (!payload.fullName || !payload.email || !payload.phone || !payload.message) {
    status.className = 'error';
    status.textContent = 'Please complete each field before sending your message.';
    return;
  }
  button.disabled = true;
  button.textContent = 'Sending your message…';
  status.className = '';
  status.textContent = '';
  try {
    const response = await fetch('/api/collected-data', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    if (!response.ok) throw new Error('Submission failed');
    form.reset();
    status.textContent = 'Thank you! Your message has been received. We look forward to talking with you.';
  } catch {
    status.className = 'error';
    status.textContent = 'We couldn’t send your message. Your details are still here—please try again shortly.';
  } finally {
    button.disabled = false;
    button.innerHTML = 'Start the conversation <span aria-hidden="true">↗</span>';
  }
});
