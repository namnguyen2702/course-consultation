// Keep standard server forms, while restoring the admin's viewing position.
(() => {
    const key = 'codepath-admin:' + window.location.pathname;
    const read = () => {
        try { return JSON.parse(sessionStorage.getItem(key) || '{}'); }
        catch { return {}; }
    };
    const write = (state) => {
        try { sessionStorage.setItem(key, JSON.stringify(state)); }
        catch { /* Forms still work when browser storage is unavailable. */ }
    };
    const panel = document.querySelector('.create-panel');
    const saved = read();
    const fields = [...document.querySelectorAll('[data-filter]')];
    const rows = [...document.querySelectorAll('[data-booking-row]')];
    const consultant = document.querySelector('[data-filter="consultant"]');

    if (consultant) {
        [...new Set(rows.map(row => row.dataset.consultant))]
            .sort((a, b) => a.localeCompare(b, 'vi'))
            .forEach(name => consultant.add(new Option(name, name)));
    }
    fields.forEach(field => {
        field.value = saved.filters?.[field.dataset.filter] || '';
    });
    const filterValues = () => Object.fromEntries(fields.map(field => [field.dataset.filter, field.value]));
    const normalize = (text) => text.normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/đ/g, 'd').toLowerCase();
    const applyFilters = () => {
        const filters = filterValues();
        let visible = 0;
        rows.forEach(row => {
            const matches = (!filters.search || normalize(row.textContent).includes(normalize(filters.search)))
                && (!filters.status || row.dataset.status === filters.status)
                && (!filters.date || row.dataset.date === filters.date)
                && (!filters.consultant || row.dataset.consultant === filters.consultant);
            row.hidden = !matches;
            if (matches) visible++;
        });
        const count = document.getElementById('result-count');
        if (count) count.textContent = `Hiển thị ${visible} / ${rows.length} lịch hẹn`;
        const empty = document.getElementById('no-filter-results');
        if (empty) empty.hidden = visible > 0 || rows.length === 0;
    };
    const savePosition = () => write({
        filters: filterValues(),
        scrollY: window.scrollY,
        restore: true
    });
    fields.forEach(field => field.addEventListener('input', () => {
        applyFilters();
        write({ ...read(), filters: filterValues() });
    }));
    document.querySelector('[data-reset-filters]')?.addEventListener('click', () => {
        fields.forEach(field => { field.value = ''; });
        applyFilters();
        write({ ...read(), filters: filterValues() });
    });
    applyFilters();

    document.querySelectorAll('.admin-workspace > p[role="status"], .admin-workspace > p.error').forEach(notice => {
        const close = document.createElement('button');
        close.type = 'button';
        close.className = 'close-notice';
        close.setAttribute('aria-label', 'Đóng thông báo');
        close.textContent = '×';
        close.addEventListener('click', () => notice.remove());
        notice.append(close);
    });

    if (panel?.querySelector('[role="alert"]') || (panel && document.querySelector('main > .error'))) {
        panel.open = true;
        requestAnimationFrame(() => panel.scrollIntoView({ block: 'start' }));
    } else if (saved.restore) {
        requestAnimationFrame(() => requestAnimationFrame(() => window.scrollTo(0, saved.scrollY || 0)));
        write({ ...saved, restore: false });
    }
    document.querySelector('[data-close-create]')?.addEventListener('click', () => {
        panel.open = false;
        panel.querySelector('summary').focus();
    });
    document.addEventListener('submit', event => {
        if (event.defaultPrevented) return;
        const form = event.target;
        if (!form.action.includes('/api/auth/logout') && !window.location.pathname.endsWith('/edit')) {
            savePosition();
        }
        if (form.method.toLowerCase() === 'post') {
            form.querySelectorAll('button[type="submit"]').forEach(button => { button.disabled = true; });
        }
    });
    document.querySelectorAll('a[href*="/edit"]').forEach(link => link.addEventListener('click', savePosition));
})();