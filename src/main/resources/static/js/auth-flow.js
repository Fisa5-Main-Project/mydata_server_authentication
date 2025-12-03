document.addEventListener("DOMContentLoaded", () => {

    // JS 변수: 최종 CI 값과 SMS 인증 ID를 저장할 곳
    let finalCi = null;
    let verificationId = null;

    // --- 1. 페이지 섹션 DOM 참조 ---
    const pages = {
        terms: document.getElementById('page-section-terms'),
        identity: document.getElementById('page-section-identity'),
        loading: document.getElementById('page-section-loading'),
        certificate: document.getElementById('page-section-cert-select'),
    };

    // --- 2. 페이지 1 (약관) 로직 ---
    const checkAll = document.getElementById('check-all');
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
    const timerEl = document.getElementById('timer');

    function validateIdentityInputs() {
        const nameValid = inputName.value.trim().length > 1;
        const rrn1Valid = /^\d{6}$/.test(inputRrn1.value);
        const rrn2Valid = /^\d{1}$/.test(inputRrn2.value);
        const carrierValid = inputCarrier.value.trim().length > 0;
        const phoneValid = /^\d{10,11}$/.test(inputPhone.value);

        const allValid = nameValid && rrn1Valid && rrn2Valid && carrierValid && phoneValid;
        btnRequestSms.disabled = !allValid;
    }

    if (identityInputs.length > 0) {
        identityInputs.forEach(input => {
            input.addEventListener('input', validateIdentityInputs);
        });

        btnRequestSms.addEventListener('click', async () => {
            try {
                const payload = {
                    name: inputName.value,
                    rrn: `${inputRrn1.value}-${inputRrn2.value}`,
                    telecom: inputCarrier.value,
                    phoneNumber: inputPhone.value
                };

                const response = await fetch('/sms/send', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });

                const result = await response.json();

                if (!response.ok || !result.isSuccess) {
                    throw new Error(result.error?.message || 'SMS 발송에 실패했습니다.');
                }

                verificationId = result.data;

                // console.log('SMS 발송 성공. verificationId:', verificationId);
                alert('인증번호가 발송되었습니다. 문자를 확인해주세요.');

                btnRequestSms.textContent = '다시 요청';
                smsCodeGroup.classList.remove('hidden');
                startTimer(180, btnRequestSms, validateIdentityInputs);
            } catch (error) {
                console.error('SMS 요청 실패:', error);
                alert(`오류가 발생했습니다: ${error.message}`);
            }
        });

        inputSmsCode.addEventListener('input', () => {
            const smsCodeValid = /^\d{6}$/.test(inputSmsCode.value);
            btnIdentityNext.disabled = !smsCodeValid;
        });

        btnIdentityNext.addEventListener('click', async () => {
            if (!verificationId) {
                alert('인증번호를 먼저 요청해주세요.');
                return;
            }

            try {
                const confirmPayload = {
                    verificationId: verificationId,
                    authCode: inputSmsCode.value
                };

                const response = await fetch('/sms/confirm', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(confirmPayload)
                });

                const result = await response.json();

                if (!response.ok || !result.isSuccess) {
                    throw new Error(result.error?.message || '인증번호가 일치하지 않습니다.');
                }

                alert(result.data); // "인증번호가 일치합니다."
                
                if (pages.loading) {
                    showPage('loading');
                    fetchCertificateData();
                }

            } catch (error) {
                console.error('SMS 확인 실패:', error);
                alert(`인증 실패: ${error.message}`);
            }
        });
    }

    function formatMaskedPhone(phone) {
        if (!phone) return '';
        // 숫자만 추출
        const clean = phone.replace(/\D/g, '');
        if (clean.length < 10) return clean;

        // 010-XXXX-YYYY 형태 가정
        const p1 = clean.substring(0, 3);
        const p2 = clean.substring(3, 7);
        const p3 = clean.substring(7);

        // 가운데 2자리, 끝 2자리 마스킹 처리 예시
        const maskedP2 = p2.substring(0, 2) + '**';
        const maskedP3 = p3.substring(0, 2) + '**';

        return `${p1}-${maskedP2}-${maskedP3}`;
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

        const loadingInterval = startFakeLoadingProgress();

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

            const res = await response.json();
            finalCi = res.data.ci;

            // (1) 상단 텍스트 이름 ("홍길동"님의 금융인증서비스)
            const topNameEl = document.querySelector('.cert-header-text .highlight');
            if (topNameEl) {
                // 서버 데이터가 없으면 입력한 값(inputName)을 사용
                topNameEl.textContent = res.data.userName || inputName.value;
            }

            // (2) 카드 내부 타이틀 ("홍길동"님의 금융인증서)
            const cardTitleEl = document.querySelector('.user-title');
            if (cardTitleEl) {
                const userName = res.data.userName || inputName.value;

                cardTitleEl.innerHTML = `<span class="name-bold">${userName}</span>님의<br>금융인증서 <span class="star-icon">★</span>`;
            }

            // (3) 전화번호 (마스킹 처리)
            const phoneEl = document.querySelector('.card-info .info-row:nth-child(1) .value');
            if (phoneEl) {
                const rawPhone = res.data.phoneNumber || inputPhone.value;
                phoneEl.textContent = formatMaskedPhone(rawPhone);
            }

            clearInterval(loadingInterval);
            showPage('certificate');

        } catch (error) {
            console.error('인증서 불러오기 실패:', error);
            clearInterval(loadingInterval);
            alert(`본인 확인에 실패했습니다: ${error.message}`);
            showPage('identity');
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
            sendCiToServer(finalCi);
        });
    }

    // --- 6. 유틸리티 함수 ---
    function showPage(pageIdToShow) {
        Object.keys(pages).forEach(pageId => {
            const pageElement = pages[pageId];
            if (pageElement) {
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

    function startTimer(durationSeconds, btnRequest, validationFn) {
        let timer = durationSeconds;
        btnRequest.disabled = true;

        const intervalId = setInterval(() => {
            let minutes = Math.floor(timer / 60);
            let seconds = timer % 60;
            if(timerEl) timerEl.textContent = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;

            if (--timer < 0) {
                clearInterval(intervalId);
                if(timerEl) timerEl.textContent = '시간만료';
                if(btnRequest) validationFn();
            }
        }, 1000);
    }

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