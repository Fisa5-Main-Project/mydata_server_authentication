// --- [oauth2-consent-page.html] 전용 스크립트 ---

const checkAll = document.getElementById('check-all');
// UI용 '필수' 체크박스들
const requiredChecks = document.querySelectorAll('.required-check');
// '전체 동의'를 포함한 UI용 모든 체크박스들
const allUiChecks = document.querySelectorAll('.check-input');

const allowButton = document.getElementById('allow-button');
const denyButton = document.getElementById('deny-button');
const form = document.getElementById('consent-form');
// (hidden) scope input들
const scopeInputs = document.querySelectorAll('.scope-input');

function updateButtonState() {
    // (UI용) '필수' 항목이 모두 체크되었는지 확인
    const allRequiredChecked = Array.from(requiredChecks).every(c => c.checked);
    allowButton.disabled = !allRequiredChecked;
}

// [UI] 전체 동의 클릭
checkAll.addEventListener('change', (e) => {
    allUiChecks.forEach(check => {
        check.checked = e.target.checked;
    });
    updateButtonState();
});

// [UI] 개별 동의 클릭
allUiChecks.forEach(check => {
    if (check.id !== 'check-all') {
        check.addEventListener('change', () => {
            const allChecked = Array.from(requiredChecks).every(c => c.checked);
            checkAll.checked = allChecked;
            updateButtonState();
        });
    }
});

// [OAuth 기능] 거부 버튼 클릭 시
denyButton.addEventListener('click', (e) => {
    e.preventDefault();
    // (hidden) scope input들을 모두 제거
    scopeInputs.forEach(input => {
        input.remove();
    });
    form.submit(); // client_id와 state만 전송
});

// [OAuth 기능] 허용 버튼 클릭 시
// 'allowButton'은 type="submit"이므로,
// disabled가 아닐 때 클릭되면 폼이 '그대로' 전송됨.
// 즉, (hidden) scope input이 모두 포함되어 전송됨.

// 초기 상태 업데이트
updateButtonState();