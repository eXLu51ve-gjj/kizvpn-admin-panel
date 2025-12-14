# 📝 Инструкция по подготовке публичного репозитория

## English / Русский

This guide explains how to prepare a public version of the repository.

Это руководство объясняет, как подготовить публичную версию репозитория.

---

## ✅ Checklist / Чеклист

### 1. Remove sensitive data / Удалить чувствительные данные

- [ ] Remove all API URLs from code / Удалить все API URL из кода
- [ ] Remove IP addresses / Удалить IP адреса
- [ ] Remove JWT tokens / Удалить JWT токены
- [ ] Replace with placeholders / Заменить на плейсхолдеры

**Files to check / Файлы для проверки:**
- `app/src/main/java/com/kizvpn/admin/data/api/ApiClient.kt`
- `app/src/main/java/com/kizvpn/admin/data/api/PasarGuardApi.kt`
- `app/src/main/java/com/kizvpn/admin/data/api/BotApi.kt`
- `CONFIG_TEMPLATE.kt`

### 2. Add screenshots / Добавить скриншоты

- [ ] Copy screenshots to `screenshots/` directory / Скопировать скриншоты в папку `screenshots/`
- [ ] Recommended screenshots / Рекомендуемые скриншоты:
  - `dashboard.png`
  - `users.png`
  - `payments.png`
  - `statistics.png`
  - `servers.png`
  - `login.png`

### 3. Build public APK / Собрать публичный APK

**Important**: Build a separate APK without sensitive data!

**Важно**: Соберите отдельный APK без чувствительных данных!

- [ ] Configure API URLs as placeholders / Настроить API URLs как плейсхолдеры
- [ ] Build APK: `./gradlew assembleRelease`
- [ ] Copy to `releases/KIZ-VPN-Panel-PUBLIC.apk`
- [ ] **Do NOT** include private APK in public repo / **НЕ** включайте приватный APK в публичный репозиторий

### 4. Update README / Обновить README

- [ ] Update `README.md` with bilingual content (done ✓)
- [ ] Update repository description on GitHub
  - English: "Mobile Admin Panel for VPN Server Management"
  - Русский: "Мобильная панель управления VPN сервером"

### 5. Remove private files / Удалить приватные файлы

Before making repository public:

Перед публикацией репозитория:

- [ ] Remove `releases/KIZ-VPN-Panel-PRIVAT.apk`
- [ ] Keep only `releases/KIZ-VPN-Panel-PUBLIC.apk`
- [ ] Remove `README_PRIVATE.md` (if contains sensitive info)
- [ ] Review all documentation files

### 6. Final checks / Финальные проверки

- [ ] Review `.gitignore` - ensure screenshots are NOT ignored
- [ ] Test public APK installation
- [ ] Verify all API endpoints use placeholders
- [ ] Check commit history for sensitive data
- [ ] Add LICENSE file (MIT recommended)

---

## 🔄 Steps to make repository public / Шаги для публикации репозитория

1. **Complete all checklist items above** / **Выполните все пункты чеклиста выше**

2. **Create a new branch for public version** / **Создайте новую ветку для публичной версии**
   ```bash
   git checkout -b public-release
   git push origin public-release
   ```

3. **Or make main branch public** / **Или сделайте main ветку публичной**
   - Go to GitHub repository settings
   - Scroll to "Danger Zone"
   - Click "Change visibility" → "Make public"

4. **Update repository description** / **Обновите описание репозитория**
   - Go to repository settings
   - Add bilingual description

---

## 📦 Public APK Requirements / Требования к публичному APK

The public APK should:
- Use placeholder API URLs (user must configure)
- Not contain any hardcoded tokens
- Not contain any real IP addresses
- Include example configuration file

Публичный APK должен:
- Использовать плейсхолдеры для API URLs (пользователь должен настроить)
- Не содержать жестко заданных токенов
- Не содержать реальных IP адресов
- Включать файл с примером конфигурации

---

## ⚠️ Important Notes / Важные примечания

- **Never commit sensitive data** / **Никогда не коммитьте чувствительные данные**
- **Review all files before making public** / **Проверьте все файлы перед публикацией**
- **Use Git history cleanup if needed** / **Используйте очистку истории Git при необходимости**
- **Consider using GitHub Secrets for CI/CD** / **Рассмотрите использование GitHub Secrets для CI/CD**

---

**Ready to publish?** / **Готовы к публикации?** 

Complete the checklist above, then make the repository public on GitHub settings.

Выполните чеклист выше, затем сделайте репозиторий публичным в настройках GitHub.

