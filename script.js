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
const humanPrompt = document.querySelector('#human-prompt');
const humanToken = document.querySelector('#human-token');
const humanAnswer = document.querySelector('#human-answer');
const verificationStatus = document.querySelector('#verification-status');
const verificationRefresh = document.querySelector('#verification-refresh');
async function loadHumanChallenge() {
  humanToken.value = '';
  humanAnswer.value = '';
  verificationRefresh.disabled = true;
  verificationStatus.textContent = 'Loading verification…';
  try {
    const response = await fetch('/api/human-verification/challenge', { cache: 'no-store' });
    const data = await response.json();
    if (!response.ok || !data.token || !data.prompt) throw new Error('Challenge unavailable');
    humanPrompt.textContent = `Quick verification: ${data.prompt}`;
    humanToken.value = data.token;
    verificationStatus.textContent = '';
  } catch {
    verificationStatus.textContent = 'Unable to load verification. Check your connection and get a new question.';
  } finally {
    verificationRefresh.disabled = false;
  }
}
verificationRefresh.addEventListener('click', loadHumanChallenge);
loadHumanChallenge();
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
  if (!payload.humanVerificationToken) {
    status.className = 'error';
    status.textContent = 'Please get a verification question before sending.';
    await loadHumanChallenge();
    return;
  }
  button.disabled = true;
  verificationRefresh.disabled = true;
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
    await loadHumanChallenge();
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
