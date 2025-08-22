# Calorie Calculator · Калькулятор калорий

A fast, bilingual (EN/RU) calorie calculator you can deploy to **GitHub Pages** in one push.

## ✨ Features
- EN/RU switch (client-side i18n)
- BMR/TDEE (Mifflin–St Jeor) and activity-based targets
- Meal planner with live totals; localStorage persistence
- Small food database (per 100 g) + your own custom foods
- PWA (installable; offline support via Service Worker)
- Ready-to-deploy **GitHub Actions** workflow for Pages

---

## 🚀 One‑push deploy to GitHub Pages

1. Create a new **public** GitHub repository (or use “Use this template” if you turned this into a template).
2. Upload all files from this folder (drag & drop in the web UI works).
3. Ensure **Actions** are enabled for the repo.
4. Push/commit to the `main` branch. The included workflow `.github/workflows/pages.yml` will build & deploy to **GitHub Pages** automatically.
5. In your repo, open the **Actions** tab → latest **Deploy to GitHub Pages** run → the published URL is shown there.

No build step needed (static site).

---

## 🧮 Formulas

**Mifflin–St Jeor BMR**  
- Male: `10*kg + 6.25*cm − 5*age + 5`  
- Female: `10*kg + 6.25*cm − 5*age − 161`

**TDEE** = `BMR × ActivityFactor`  
Activity factors: 1.2 (sedentary), 1.375, 1.55, 1.725, 1.9

Targets: maintenance = TDEE, mild cut = −10%, cut = −20%, bulk = +10%.

---

## 📦 Project structure

```
.
├─ index.html
├─ assets/
│  ├─ style.css
│  ├─ app.js
│  ├─ foods.json
│  └─ i18n/
│     ├─ en.json
│     └─ ru.json
├─ sw.js
├─ manifest.webmanifest
├─ .github/workflows/pages.yml
├─ LICENSE (MIT)
└─ README.md
```

---

## 🇷🇺 Краткая инструкция по деплою на GitHub Pages

1. Создайте **публичный** репозиторий на GitHub.
2. Загрузите все файлы из этой папки (можно просто перетащить в веб‑интерфейсе).
3. Убедитесь, что вкладка **Actions** включена для репозитория.
4. Сделайте коммит в ветку `main` — workflow `.github/workflows/pages.yml` сам задеплоит сайт на **GitHub Pages**.
5. Ссылку на опубликованный сайт смотрите во вкладке **Actions** → последний запуск **Deploy to GitHub Pages**.

Сборка не требуется — проект статический.

---

## 🔧 Local dev

Just open `index.html` in a browser, or serve the folder with any static server:
```bash
python3 -m http.server 8080
```

---

## 📝 License

MIT — do whatever, just keep the copyright and license notice.
