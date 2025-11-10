document.addEventListener("DOMContentLoaded", () => {

    // JS 변수: 최종 CI 값을 저장할 곳
    let finalCi = null;

    // --- 1. 페이지 섹션 DOM 참조 [!! 수정 !!] ---
    // HTML의 ID (page-section-...)와 일치시킴
    const pages = {
        terms: document.getElementById('page-section-terms'),
        identity: document.getElementById('page-section-identity'),
        loading: document.getElementById('page-section-loading'),
        certificate: document.getElementById('page-section-cert-select'),
    };

    // --- 2. 페이지 1 (약관) 로직 [!! 수정 !!] ---
    const checkAll = document.getElementById('check-all');
    // HTML의 클래스명 (.required-check)과 일치시킴
    const requiredChecks = pages.terms.querySelectorAll('.required-check');
    const allChecks = pages.terms.querySelectorAll('.check-input');
    const btnTermsNext = document.getElementById('btn-terms-next');

    function updateTermsButton() {
        const allRequiredChecked = Array.from(requiredChecks).every(c => c.checked);
        btnTermsNext.disabled = !allRequiredChecked;
    }
    if (checkAll && requiredChecks && allChecks && btnTermsNext) {
        allChecks.forEach(check => check.addEventListener('change', updateTermsButton));
        checkAll.addEventListener('change', (e) => {
            allChecks.forEach(check => { check.checked = e.target.checked; });
            updateTermsButton();
        });
        btnTermsNext.addEventListener('click', () => showPage('identity'));
    }


    // --- 3. 페이지 2 (본인확인) DOM 및 이벤트 ---
    const identityInputs = pages.identity.querySelectorAll('.identity-input');
    const inputName = document.getElementById('name');
    const inputRrn1 = document.getElementById('rrn1');
    const inputRrn2 = document.getElementById('rrn2');
    const inputCarrier = document.getElementById('carrier');
    const inputPhone = document.getElementById('phone');
    const btnRequestSms = document.getElementById('btn-request-sms');
    const smsCodeGroup = document.getElementById('sms-code-group');
    const inputSmsCode = document.getElementById('sms-code');
    const btnIdentityNext = document.getElementById('btn-identity-next');
    const timerEl = document.getElementById('timer'); // 타이머 요소

    function validateIdentityInputs() {
        const nameValid = inputName.value.trim().length > 1;
        const rrn1Valid = /^\d{6}$/.test(inputRrn1.value);
        const rrn2Valid = /^\d{1}$/.test(inputRrn2.value); // [수정] 1자리
        const carrierValid = inputCarrier.value.trim().length > 0;
        const phoneValid = /^\d{10,11}$/.test(inputPhone.value);

        const allValid = nameValid && rrn1Valid && rrn2Valid && carrierValid && phoneValid;
        btnRequestSms.disabled = !allValid;
    }

    if (identityInputs.length > 0) {
        identityInputs.forEach(input => {
            input.addEventListener('input', validateIdentityInputs);
        });

        btnRequestSms.addEventListener('click', () => {
            console.log('서버에 SMS 인증번호 요청');
            // (실제 fetch/AJAX로 서버에 SMS 발송 요청)
            btnRequestSms.textContent = '다시 요청';
            btnRequestSms.disabled = true;
            smsCodeGroup.classList.remove('hidden');
            startTimer(180, btnRequestSms, validateIdentityInputs); // 타이머 시작
        });

        inputSmsCode.addEventListener('input', () => {
            const smsCodeValid = /^\d{6}$/.test(inputSmsCode.value);
            btnIdentityNext.disabled = !smsCodeValid;
        });

        btnIdentityNext.addEventListener('click', () => {
            // [수정] showPage 호출 전 null 체크
            if (pages.loading) {
                showPage('loading');
                fetchCertificateData(); // 페이지 3 (로딩) API 호출
            }
        });
    }

    // --- 4. 페이지 3 (로딩) 로직 ---
    async function fetchCertificateData() {
        const payload = {
            name: inputName.value,
            rrn1: inputRrn1.value,
            rrn2: inputRrn2.value,
            carrier: inputCarrier.value,
            phone: inputPhone.value,
            smsCode: inputSmsCode.value
        };

        const loadingInterval = startFakeLoadingProgress(); // 가짜 로딩 시작

        try {
            const response = await fetch('/api/v1/cert/fetch', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                const errorBody = await response.text();
                throw new Error(`서버 통신 실패: ${response.status} ${errorBody}`);
            }

            const res = await response.json(); // FetchCertResponseDto

            console.log('서버로부터 받은 CI:', res.data.ci);
            finalCi = res.data.ci; // [중요] CI 값을 전역 변수에 저장

            // 페이지 4 UI 업데이트
            const certUI = document.querySelector('.cert-ui-placeholder');
            if(certUI) certUI.textContent = res.data.certificateName;

            clearInterval(loadingInterval); // 가짜 로딩 중지
            showPage('certificate'); // [성공] 페이지 4 표시

        } catch (error) {
            console.error('인증서 불러오기 실패:', error);
            clearInterval(loadingInterval); // 로딩 중지
            alert(`본인 확인에 실패했습니다: ${error.message}\n(Mock 데이터(홍길동/김영희)와 일치하는지 확인하세요)`);
            showPage('identity'); // 오류 시 페이지 2로 복귀
        }
    }

    function startFakeLoadingProgress() {
        const percentEl = document.getElementById('spinner-text');
        const spinner = document.querySelector('.spinner');
        let percent = 0;

        const interval = setInterval(() => {
            percent += 5;
            if (percent >= 99) {
                clearInterval(interval);
            }
            if(percentEl) percentEl.textContent = `${percent}%`;
            if(spinner) spinner.style.background =
                `conic-gradient(#FFA726 0% ${percent}%, #F0F0F0 ${percent}% 100%)`;
        }, 150);
        return interval;
    }


    // --- 5. 페이지 4 (인증서) 로직 ---
    const btnCertSelect = document.getElementById('btn-cert-select');
    if (btnCertSelect) {
        btnCertSelect.addEventListener('click', () => {
            if (!finalCi) {
                alert('오류: CI 값이 없습니다. 처음부터 다시 시도해주세요.');
                showPage('identity');
                return;
            }

            // [호출] 유틸리티 함수 sendCiToServer 호출
            sendCiToServer(finalCi);
        });
    }

    // --- 6. 유틸리티 함수 ---

    /**
     * 지정된 ID의 페이지만 보여주고 나머지는 숨김
     */
    function showPage(pageIdToShow) {
        Object.keys(pages).forEach(pageId => {
            const pageElement = pages[pageId]; // JS 객체에서 DOM 요소를 가져옴
            if (pageElement) { // [수정] 요소가 null이 아닌지 확인
                if (pageId === pageIdToShow) {
                    pageElement.classList.remove('hidden');
                } else {
                    pageElement.classList.add('hidden');
                }
            } else {
                console.warn(`[showPage] '${pageId}' ID를 가진 요소를 찾을 수 없습니다.`);
            }
        });
    }

    /**
     * 인증번호 타이머 시작
     */
    function startTimer(durationSeconds, btnRequest, validationFn) {
        let timer = durationSeconds;

        const intervalId = setInterval(() => {
            let minutes = Math.floor(timer / 60);
            let seconds = timer % 60;
            if(timerEl) timerEl.textContent = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;

            if (--timer < 0) {
                clearInterval(intervalId);
                if(timerEl) timerEl.textContent = '시간만료';
                if(btnRequest) validationFn(); // '다시 요청' 버튼 활성화 여부 재검증
            }
        }, 1000);
    }

    /**
     * [최종] CI 값을 서버로 전송 (Full Page POST)
     */
    function sendCiToServer(ciValue) {
        console.log('최종 CI 전송 (form submit):', ciValue);

        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/oauth/my-cert-callback';

        const ciInput = document.createElement('input');
        ciInput.type = 'hidden';
        ciInput.name = 'ci';
        ciInput.value = ciValue;
        form.appendChild(ciInput);

        document.body.appendChild(form);
        form.submit();
    }

    // --- 초기화 ---
    showPage('terms');
});