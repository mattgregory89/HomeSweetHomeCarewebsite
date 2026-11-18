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
const verificationStatus = document.querySelector('#verification-status');
const verificationRetry = document.querySelector('#verification-retry');
let recaptchaWidgetId = null;
let recaptchaLoading = false;
let recaptchaScriptPromise;
function loadRecaptchaScript() {
  if (window.grecaptcha?.render) return Promise.resolve();
  if (recaptchaScriptPromise) return recaptchaScriptPromise;
  recaptchaScriptPromise = new Promise((resolve, reject) => {
    const script = document.createElement('script');
    const timeout = setTimeout(() => fail(), 15000);
    function fail() {
      clearTimeout(timeout);
      script.remove();
      recaptchaScriptPromise = null;
      reject(new Error('reCAPTCHA could not load. Check your connection or content blocker and retry.'));
    }
    window.onRecaptchaLoaded = () => { clearTimeout(timeout); resolve(); };
    script.src = 'https://www.google.com/recaptcha/api.js?onload=onRecaptchaLoaded&render=explicit';
    script.async = true;
    script.defer = true;
    script.onerror = fail;
    document.head.appendChild(script);
  });
  return recaptchaScriptPromise;
}
async function loadRecaptcha() {
  if (recaptchaLoading || recaptchaWidgetId !== null) return;
  recaptchaLoading = true;
  verificationRetry.hidden = true;
  verificationStatus.textContent = 'Loading verification…';
  try {
    if (location.protocol === 'file:') {
      throw new Error('Open this site at http://localhost:8080 with the app running to use the contact form.');
    }
    const response = await fetch('/api/recaptcha/config', { cache: 'no-store' });
    const data = await response.json().catch(() => null);
    if (!response.ok || !data?.siteKey) {
      throw new Error(data?.message || 'Contact verification is unavailable. Open the site through the running app at http://localhost:8080.');
    }
    await loadRecaptchaScript();
    recaptchaWidgetId = window.grecaptcha.render('recaptcha-widget', {
      sitekey: data.siteKey,
      size: window.matchMedia('(max-width: 420px)').matches ? 'compact' : 'normal',
      callback: () => { verificationStatus.textContent = ''; },
      'expired-callback': () => { verificationStatus.textContent = 'Verification expired. Please check reCAPTCHA again.'; },
      'error-callback': () => { verificationStatus.textContent = 'reCAPTCHA encountered a connection error. Please try again.'; }
    });
    verificationStatus.textContent = '';
  } catch (error) {
    verificationStatus.textContent = error.message || 'Unable to load verification. Please retry.';
    verificationRetry.hidden = false;
  } finally {
    recaptchaLoading = false;
  }
}
verificationRetry.addEventListener('click', loadRecaptcha);
loadRecaptcha();
form.addEventListener('submit', async event => {
  event.preventDefault();
  const button = form.querySelector('button[type="submit"]');
  if (button.disabled) return;
  if (!form.reportValidity()) return;
  const status = document.querySelector('#form-status');
  const payload = Object.fromEntries(new FormData(form));
  Object.keys(payload).forEach(key => { payload[key] = payload[key].trim(); });
  if (!payload.fullName || !payload.email || !payload.phone || !payload.message) {
    status.className = 'error';
    status.textContent = 'Please complete each field before sending your message.';
    return;
  }
  delete payload['g-recaptcha-response'];
  payload.recaptchaToken = recaptchaWidgetId !== null ? window.grecaptcha.getResponse(recaptchaWidgetId) : '';
  if (!payload.recaptchaToken) {
    status.className = 'error';
    status.textContent = 'Please complete reCAPTCHA before sending.';
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
    const data = await response.json().catch(() => ({}));
    if (!response.ok) throw new Error(data.message || 'We couldn’t send your message. Please try again shortly.');
    form.reset();
    status.textContent = data.message || 'Thank you! Your message has been received.';
  } catch (error) {
    status.className = 'error';
    status.textContent = `${error.message || 'Unable to send your message.'} Your details are still here.`;
  } finally {
    if (recaptchaWidgetId !== null) window.grecaptcha.reset(recaptchaWidgetId);
    button.disabled = false;
    button.innerHTML = 'Start the conversation <span aria-hidden="true">↗</span>';
  }
});

const assistantMessages = document.querySelector("#assistant-messages");
const assistantInput = document.querySelector("#assistant-input");
const assistantSend = document.querySelector("#assistant-send");
      const assistantKnowledge = [
        {
          keywords: ["respond", "response", "call back", "follow up"],
          answer: "Most requests are answered within one business day.",
        },
        {
          keywords: ["custom", "plan", "care plan"],
          answer: "Yes, we create custom care plans based on your needs and family preferences.",
        },
        {
          keywords: ["minimum", "hours"],
          answer: "There is no fixed minimum. We review flexible care-hour options during consultation.",
        },
        {
          keywords: ["schedule", "scheduling", "appointment"],
          answer: "Use the scheduling page or the contact form and we will help coordinate a consultation.",
        },
        {
          keywords: ["service", "services"],
          answer: "We provide companionship, personal care, respite support, and custom in-home care services.",
        },
      ];

      function addAssistantMessage(text, role) {
        const message = document.createElement("p");
        message.className = `assistant-message ${role}`;
        message.textContent = text;
        assistantMessages.appendChild(message);
        assistantMessages.scrollTop = assistantMessages.scrollHeight;
      }

      function findAssistantAnswer(question) {
        const normalizedQuestion = question.toLowerCase();
        const match = assistantKnowledge.find((entry) =>
          entry.keywords.some((keyword) => normalizedQuestion.includes(keyword))
        );
        return (
          match?.answer ||
          "I can help with FAQ topics, scheduling, and service questions. You can also submit the contact form for direct support."
        );
      }

      function handleAssistantSubmit() {
        const question = assistantInput.value.trim();
        if (!question) return;
        addAssistantMessage(`You: ${question}`, "user");
        addAssistantMessage(`Assistant: ${findAssistantAnswer(question)}`, "bot");
        assistantInput.value = "";
      }

      assistantSend.addEventListener("click", handleAssistantSubmit);
      assistantInput.addEventListener("keydown", (event) => {
        if (event.key === "Enter") {
          event.preventDefault();
          handleAssistantSubmit();
        }
      });


addAssistantMessage("Assistant: Hi! Ask me about services, scheduling, or FAQs.", "bot");

const assistantToggle = document.querySelector('#assistant-toggle');
const assistantPanel = document.querySelector('#assistant-panel');
const assistantClose = document.querySelector('#assistant-close');
function setAssistantOpen(open) {
  assistantPanel.hidden = !open;
  assistantToggle.setAttribute('aria-expanded', String(open));
  assistantToggle.setAttribute('aria-label', open ? 'Close Care Assistant' : 'Open Care Assistant');
  if (open) {
    assistantMessages.scrollTop = assistantMessages.scrollHeight;
    assistantInput.focus();
  } else {
    assistantToggle.focus();
  }
}
assistantToggle.addEventListener('click', () => setAssistantOpen(assistantPanel.hidden));
assistantClose.addEventListener('click', () => setAssistantOpen(false));
assistantPanel.addEventListener('keydown', event => {
  if (event.key === 'Escape') {
    event.preventDefault();
    setAssistantOpen(false);
  }
});
