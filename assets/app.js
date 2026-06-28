// ── i18n ────────────────────────────────────────────────────────────────────
let translations = {};

async function loadLang(code) {
  const res = await fetch(`assets/i18n/${code}.json`);
  translations = await res.json();
  applyTranslations();
  localStorage.setItem('lang', code);
}

function applyTranslations() {
  Object.entries(translations).forEach(([id, text]) => {
    const el = document.getElementById(id);
    if (!el) return;
    if (el.hasAttribute('placeholder')) {
      el.placeholder = text;
    } else {
      el.textContent = text;
    }
  });
}

// ── Tabs ─────────────────────────────────────────────────────────────────────
document.querySelectorAll('.tab').forEach(btn => {
  btn.addEventListener('click', () => {
    document.querySelectorAll('.tab').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.panel').forEach(p => p.classList.remove('active'));
    btn.classList.add('active');
    document.getElementById('panel-' + btn.dataset.tab).classList.add('active');
  });
});

// ── BMR / TDEE ───────────────────────────────────────────────────────────────
document.getElementById('bmrForm').addEventListener('submit', e => {
  e.preventDefault();
  const form = e.target;
  const sex = form.querySelector('[name=sex]:checked').value;
  const age = +document.getElementById('ageInput').value;
  const units = form.querySelector('[name=units]:checked').value;
  let weight = +document.getElementById('weightInput').value;
  let height = +document.getElementById('heightInput').value;

  if (units === 'imperial') {
    weight *= 0.453592;   // lb → kg
    height *= 2.54;       // in → cm
  }

  const bmr = sex === 'male'
    ? 10 * weight + 6.25 * height - 5 * age + 5
    : 10 * weight + 6.25 * height - 5 * age - 161;

  const tdee = bmr * +document.getElementById('activitySelect').value;

  document.getElementById('bmrVal').textContent      = Math.round(bmr);
  document.getElementById('tdeeVal').textContent     = Math.round(tdee);
  document.getElementById('maintVal').textContent    = Math.round(tdee);
  document.getElementById('mildLossVal').textContent = Math.round(tdee * 0.9);
  document.getElementById('lossVal').textContent     = Math.round(tdee * 0.8);
  document.getElementById('gainVal').textContent     = Math.round(tdee * 1.1);
  document.getElementById('bmrResults').hidden = false;
});

// ── Food database ─────────────────────────────────────────────────────────────
let builtinFoods = [];
let customFoods  = JSON.parse(localStorage.getItem('customFoods') || '[]');

async function loadFoods() {
  const res = await fetch('assets/foods.json');
  builtinFoods = await res.json();
  renderFoodsGrid(allFoods());
  updateDatalist();
}

function allFoods() {
  return [...builtinFoods, ...customFoods];
}

function renderFoodsGrid(list) {
  const grid = document.getElementById('foodsGrid');
  grid.innerHTML = '';
  if (list.length === 0) {
    grid.innerHTML = '<p style="color:var(--muted);grid-column:1/-1">No foods found.</p>';
    return;
  }
  list.forEach(food => {
    const card = document.createElement('div');
    card.className = 'food-card';
    card.innerHTML =
      `<div class="name">${escHtml(food.name)}</div>` +
      `<div class="meta">` +
        `<span>${food.kcal} kcal</span>` +
        `<span>P ${food.protein}g</span>` +
        `<span>F ${food.fat}g</span>` +
        `<span>C ${food.carbs}g</span>` +
      `</div>`;
    card.addEventListener('click', () => {
      document.getElementById('addFoodInput').value = food.name;
      document.querySelector('.tab[data-tab="planner"]').click();
    });
    grid.appendChild(card);
  });
}

document.getElementById('foodSearch').addEventListener('input', e => {
  const q = e.target.value.toLowerCase();
  renderFoodsGrid(allFoods().filter(f => f.name.toLowerCase().includes(q)));
});

document.getElementById('customFoodForm').addEventListener('submit', e => {
  e.preventDefault();
  const food = {
    name:    document.getElementById('cfName').value.trim(),
    kcal:    +document.getElementById('cfCal').value,
    protein: +document.getElementById('cfProt').value,
    fat:     +document.getElementById('cfFat').value,
    carbs:   +document.getElementById('cfCarb').value,
  };
  customFoods.push(food);
  localStorage.setItem('customFoods', JSON.stringify(customFoods));
  e.target.reset();
  renderFoodsGrid(allFoods());
  updateDatalist();
});

// ── Meal planner ──────────────────────────────────────────────────────────────
let plan = JSON.parse(localStorage.getItem('plan') || '[]');

function updateDatalist() {
  const dl = document.getElementById('foodList');
  dl.innerHTML = allFoods()
    .map(f => `<option value="${escAttr(f.name)}">`)
    .join('');
}

function calcMacros(food, grams) {
  const ratio = grams / 100;
  return {
    kcal:    Math.round(food.kcal    * ratio),
    protein: +(food.protein * ratio).toFixed(1),
    fat:     +(food.fat     * ratio).toFixed(1),
    carbs:   +(food.carbs   * ratio).toFixed(1),
  };
}

function renderPlanner() {
  const tbody = document.getElementById('plannerBody');
  tbody.innerHTML = '';
  let totCal = 0, totProt = 0, totFat = 0, totCarb = 0;

  plan.forEach((item, i) => {
    const m = calcMacros(item, item.grams);
    totCal  += m.kcal;
    totProt += m.protein;
    totFat  += m.fat;
    totCarb += m.carbs;

    const tr = document.createElement('tr');
    tr.innerHTML =
      `<td>${escHtml(item.name)}</td>` +
      `<td>${item.grams}</td>` +
      `<td>${m.kcal}</td>` +
      `<td>${m.protein} g</td>` +
      `<td>${m.fat} g</td>` +
      `<td>${m.carbs} g</td>` +
      `<td><button class="btn danger small" data-i="${i}">✕</button></td>`;
    tbody.appendChild(tr);
  });

  tbody.querySelectorAll('[data-i]').forEach(btn => {
    btn.addEventListener('click', () => {
      plan.splice(+btn.dataset.i, 1);
      renderPlanner();
    });
  });

  document.getElementById('totalCal').textContent  = totCal;
  document.getElementById('totalProt').textContent = totProt.toFixed(1);
  document.getElementById('totalFat').textContent  = totFat.toFixed(1);
  document.getElementById('totalCarb').textContent = totCarb.toFixed(1);
}

document.getElementById('addItemBtn').addEventListener('click', () => {
  const name  = document.getElementById('addFoodInput').value.trim();
  const grams = +document.getElementById('addGramsInput').value;
  if (!name || !grams || grams <= 0) return;
  const food = allFoods().find(f => f.name.toLowerCase() === name.toLowerCase());
  if (!food) {
    alert(translations['foodNotFound'] || `"${name}" not found in database.`);
    return;
  }
  plan.push({ ...food, grams });
  document.getElementById('addFoodInput').value = '';
  renderPlanner();
});

document.getElementById('savePlanBtn').addEventListener('click', () => {
  localStorage.setItem('plan', JSON.stringify(plan));
});

document.getElementById('clearPlanBtn').addEventListener('click', () => {
  if (plan.length === 0) return;
  plan = [];
  localStorage.removeItem('plan');
  renderPlanner();
});

// ── PWA install prompt ────────────────────────────────────────────────────────
let deferredInstallPrompt = null;

window.addEventListener('beforeinstallprompt', e => {
  e.preventDefault();
  deferredInstallPrompt = e;
  document.getElementById('installBtn').hidden = false;
});

document.getElementById('installBtn').addEventListener('click', () => {
  if (!deferredInstallPrompt) return;
  deferredInstallPrompt.prompt();
  deferredInstallPrompt.userChoice.then(() => {
    document.getElementById('installBtn').hidden = true;
    deferredInstallPrompt = null;
  });
});

// ── Language switcher ─────────────────────────────────────────────────────────
document.getElementById('langSelect').addEventListener('change', e => {
  loadLang(e.target.value);
});

// ── Helpers ───────────────────────────────────────────────────────────────────
function escHtml(str) {
  return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function escAttr(str) {
  return str.replace(/"/g, '&quot;').replace(/'/g, '&#39;');
}

// ── Boot ──────────────────────────────────────────────────────────────────────
(function init() {
  const savedLang = localStorage.getItem('lang') || 'en';
  document.getElementById('langSelect').value = savedLang;
  loadLang(savedLang);
  loadFoods();
  renderPlanner();
})();
