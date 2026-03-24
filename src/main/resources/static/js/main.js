// ═══════════════════════════════════════════
//  HotBake — Main JavaScript
// ═══════════════════════════════════════════

document.addEventListener('DOMContentLoaded', () => {

  // ── User dropdown ──────────────────────────
  const userMenuBtn  = document.getElementById('userMenuBtn');
  const userDropdown = document.getElementById('userDropdown');
  if (userMenuBtn && userDropdown) {
    userMenuBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      userDropdown.classList.toggle('open');
    });
    document.addEventListener('click', () => userDropdown.classList.remove('open'));
  }

  // ── Product page: quantity calculator ──────
  const productQtyInput = document.getElementById('quantityInput');
  const productTotalDisplay = document.getElementById('totalPriceDisplay');
  const productPricePerUnit = parseFloat(document.getElementById('pricePerPound')?.value || 0);
  const productByPieces = document.getElementById('isByPieces')?.value === 'true';
  const productIncreaseBtn = document.querySelector('.product-qty-increase');
  const productDecreaseBtn = document.querySelector('.product-qty-decrease');

  if (productQtyInput && productTotalDisplay && productPricePerUnit) {
    const updateProductTotal = () => {
      const qty = parseFloat(productQtyInput.value) || 0;
      productTotalDisplay.textContent = '৳ ' + (qty * productPricePerUnit).toFixed(2);
    };

    const changeProductQty = (delta) => {
      const current = parseFloat(productQtyInput.value) || 0;
      if (productByPieces) {
        const newVal = Math.max(1, current + delta);
        productQtyInput.value = String(Math.floor(newVal));
      } else {
        const newVal = Math.max(0.5, current + delta);
        productQtyInput.value = newVal.toFixed(1);
      }
      updateProductTotal();
    };

    productIncreaseBtn?.addEventListener('click', () => {
      changeProductQty(productByPieces ? 1 : 0.5);
    });
    productDecreaseBtn?.addEventListener('click', () => {
      changeProductQty(productByPieces ? -1 : -0.5);
    });

    updateProductTotal();
  }

  // ── Image gallery (product detail) ─────────
  const thumbs   = document.querySelectorAll('.thumb');
  const mainImg  = document.getElementById('mainProductImage');
  thumbs.forEach(thumb => {
    thumb.addEventListener('click', () => {
      thumbs.forEach(t => t.classList.remove('active'));
      thumb.classList.add('active');
      if (mainImg) mainImg.src = thumb.dataset.src;
    });
  });

  // ── Star rating selector ────────────────────
  const stars = document.querySelectorAll('.star-input');
  const ratingInput = document.getElementById('ratingValue');
  if (stars.length && ratingInput) {
    stars.forEach(star => {
      star.addEventListener('click', () => {
        const val = parseInt(star.dataset.value);
        if (ratingInput) ratingInput.value = val;
        stars.forEach(s => {
          s.classList.toggle('filled', parseInt(s.dataset.value) <= val);
          s.style.color = parseInt(s.dataset.value) <= val ? 'hsl(43, 96%, 56%)' : 'hsl(213, 27%, 84%)';
        });
      });
      star.addEventListener('mouseover', () => {
        const val = parseInt(star.dataset.value);
        stars.forEach(s => {
          s.style.color = parseInt(s.dataset.value) <= val ? 'hsl(43, 96%, 56%)' : 'hsl(213, 27%, 84%)';
        });
      });
      star.addEventListener('mouseout', () => {
        const current = parseInt(ratingInput?.value || 0);
        stars.forEach(s => {
          s.style.color = parseInt(s.dataset.value) <= current ? 'hsl(43, 96%, 56%)' : 'hsl(213, 27%, 84%)';
        });
      });
    });
  }

  // ── Image upload preview ────────────────────
  const imageInput    = document.getElementById('imageUploadInput');
  const previewsWrap  = document.getElementById('imagePreviews');
  const uploadZone    = document.getElementById('uploadZone');
  if (imageInput && previewsWrap) {
    imageInput.addEventListener('change', () => showPreviews(imageInput.files));
  }
  if (uploadZone && imageInput) {
    uploadZone.addEventListener('click', () => imageInput.click());
    uploadZone.addEventListener('dragover', e => { e.preventDefault(); uploadZone.classList.add('dragover'); });
    uploadZone.addEventListener('dragleave', () => uploadZone.classList.remove('dragover'));
    uploadZone.addEventListener('drop', e => {
      e.preventDefault();
      uploadZone.classList.remove('dragover');
      imageInput.files = e.dataTransfer.files;
      showPreviews(e.dataTransfer.files);
    });
  }
  function showPreviews(files) {
    previewsWrap.innerHTML = '';
    Array.from(files).forEach(file => {
      const reader = new FileReader();
      reader.onload = e => {
        const img = document.createElement('img');
        img.src = e.target.result;
        img.className = 'upload-preview-img';
        previewsWrap.appendChild(img);
      };
      reader.readAsDataURL(file);
    });
  }

  // ── Cart quantity controls ──────────────────
  document.querySelectorAll('.qty-control .qty-increase').forEach(btn => {
    btn.addEventListener('click', () => {
      const formWrap = btn.closest('.qty-control');
      if (!formWrap) return;
      const input = formWrap.querySelector('input');
      if (!input) return;
      const step  = parseFloat(input.step) || 0.5;
      input.value = (parseFloat(input.value) + step).toFixed(1);
      autoSubmitCart(input);
    });
  });
  document.querySelectorAll('.qty-control .qty-decrease').forEach(btn => {
    btn.addEventListener('click', () => {
      const formWrap = btn.closest('.qty-control');
      if (!formWrap) return;
      const input = formWrap.querySelector('input');
      if (!input) return;
      const step  = parseFloat(input.step) || 0.5;
      const newVal = parseFloat(input.value) - step;
      input.value = (newVal < 0.5 ? 0.5 : newVal).toFixed(1);
      autoSubmitCart(input);
    });
  });
  function autoSubmitCart(input) {
    const form = input.closest('form');
    if (form) form.submit();
  }

  // ── Checkout: saved address selector ────────
  const addressRadios = document.querySelectorAll('.address-radio');
  const newAddressForm = document.getElementById('newAddressForm');
  addressRadios.forEach(r => {
    r.addEventListener('change', () => {
      if (newAddressForm) {
        newAddressForm.style.display = r.value === 'new' ? 'block' : 'none';
      }
    });
  });

  // ── Confirm dialogs ─────────────────────────
  document.querySelectorAll('[data-confirm]').forEach(el => {
    el.addEventListener('click', e => {
      if (!confirm(el.dataset.confirm)) e.preventDefault();
    });
  });

  // ── Flash auto-dismiss ───────────────────────
  document.querySelectorAll('.flash').forEach(flash => {
    setTimeout(() => flash.remove(), 5000);
  });

  // ── Seller reject modal ──────────────────────
  document.querySelectorAll('.reject-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const sellerId = btn.dataset.sellerId;
      const modal    = document.getElementById('rejectModal');
      if (modal) {
        modal.querySelector('[name="sellerId"]').value = sellerId;
        modal.style.display = 'flex';
      }
    });
  });
  document.getElementById('rejectModalClose')?.addEventListener('click', () => {
    document.getElementById('rejectModal').style.display = 'none';
  });
  window.addEventListener('click', e => {
    const modal = document.getElementById('rejectModal');
    if (modal && e.target === modal) modal.style.display = 'none';
  });

});
