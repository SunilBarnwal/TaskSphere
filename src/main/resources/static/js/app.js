(function () {
    function ready(fn) {
        if (document.readyState !== "loading") {
            fn();
            return;
        }
        document.addEventListener("DOMContentLoaded", fn);
    }

    function enhanceTables() {
        document.querySelectorAll("[data-enhanced-table]").forEach(function (tableWrapper) {
            var table = tableWrapper.querySelector("table");
            if (!table) {
                return;
            }

            var tbody = table.querySelector("tbody");
            var rows = Array.from(tbody.querySelectorAll("tr"));
            var searchInput = tableWrapper.querySelector("[data-table-search]");
            var pageSizeSelect = tableWrapper.querySelector("[data-page-size]");
            var pagination = tableWrapper.querySelector("[data-pagination]");
            var sortableHeaders = Array.from(table.querySelectorAll("[data-sort-key]"));
            var state = {
                search: "",
                sortKey: "",
                sortDirection: "asc",
                page: 1,
                pageSize: Number(pageSizeSelect ? pageSizeSelect.value : 5)
            };

            function valueForSort(row, key) {
                var cell = row.querySelector("[data-value-" + key + "]");
                return (cell ? cell.getAttribute("data-value-" + key) : row.innerText).toLowerCase();
            }

            function apply() {
                var filtered = rows.filter(function (row) {
                    return row.innerText.toLowerCase().includes(state.search);
                });

                if (state.sortKey) {
                    filtered.sort(function (a, b) {
                        var aValue = valueForSort(a, state.sortKey);
                        var bValue = valueForSort(b, state.sortKey);
                        if (aValue < bValue) {
                            return state.sortDirection === "asc" ? -1 : 1;
                        }
                        if (aValue > bValue) {
                            return state.sortDirection === "asc" ? 1 : -1;
                        }
                        return 0;
                    });
                }

                var totalPages = Math.max(1, Math.ceil(filtered.length / state.pageSize));
                state.page = Math.min(state.page, totalPages);
                var start = (state.page - 1) * state.pageSize;
                var visibleRows = filtered.slice(start, start + state.pageSize);

                rows.forEach(function (row) {
                    row.style.display = "none";
                });
                visibleRows.forEach(function (row) {
                    row.style.display = "";
                });

                if (pagination) {
                    pagination.innerHTML = "";
                    for (var i = 1; i <= totalPages; i += 1) {
                        var button = document.createElement("button");
                        button.type = "button";
                        button.className = "btn btn-sm " + (i === state.page ? "btn-primary" : "btn-outline-secondary");
                        button.textContent = String(i);
                        button.addEventListener("click", function (event) {
                            state.page = Number(event.target.textContent);
                            apply();
                        });
                        pagination.appendChild(button);
                    }
                }
            }

            if (searchInput) {
                searchInput.addEventListener("input", function (event) {
                    state.search = event.target.value.trim().toLowerCase();
                    state.page = 1;
                    apply();
                });
            }

            if (pageSizeSelect) {
                pageSizeSelect.addEventListener("change", function (event) {
                    state.pageSize = Number(event.target.value);
                    state.page = 1;
                    apply();
                });
            }

            sortableHeaders.forEach(function (header) {
                header.style.cursor = "pointer";
                header.addEventListener("click", function () {
                    var key = header.getAttribute("data-sort-key");
                    if (state.sortKey === key) {
                        state.sortDirection = state.sortDirection === "asc" ? "desc" : "asc";
                    } else {
                        state.sortKey = key;
                        state.sortDirection = "asc";
                    }
                    apply();
                });
            });

            apply();
        });
    }

    function initSidebar() {
        const body = document.body;
        const toggle = document.querySelector("[data-sidebar-toggle]");

        if (!toggle) return;

        toggle.addEventListener("click", function (e) {
            e.stopPropagation();

            if (window.innerWidth < 992) {
                body.classList.toggle("sidebar-open");
            } else {
                body.classList.toggle("sidebar-collapsed");
            }
        });
    }

    function initToasts() {
        var toastContainer = document.querySelector("[data-toast-container]");
        if (!toastContainer) {
            return;
        }
        document.querySelectorAll("[data-toast-message]").forEach(function (source) {
            var toast = document.createElement("div");
            toast.className = "toast align-items-center border-0 show";
            toast.role = "alert";
            toast.innerHTML =
                '<div class="d-flex">' +
                '<div class="toast-body">' + source.getAttribute("data-toast-message") + "</div>" +
                '<button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>' +
                "</div>";
            toast.classList.add(source.getAttribute("data-toast-level") === "error" ? "text-bg-danger" : "text-bg-success");
            toastContainer.appendChild(toast);
            setTimeout(function () {
                toast.remove();
            }, 4000);
        });
    }

    function initConfirmation() {
        var confirmModal = document.getElementById("confirmationModal");
        if (!confirmModal || typeof bootstrap === "undefined") {
            return;
        }
        var modal = new bootstrap.Modal(confirmModal);
        var confirmText = confirmModal.querySelector("[data-confirm-text]");
        var confirmButton = confirmModal.querySelector("[data-confirm-accept]");
        var pendingForm = null;

        document.querySelectorAll("[data-confirm]").forEach(function (button) {
            button.addEventListener("click", function (event) {
                event.preventDefault();
                pendingForm = button.closest("form");
                confirmText.textContent = button.getAttribute("data-confirm");
                modal.show();
            });
        });

        confirmButton.addEventListener("click", function () {
            if (pendingForm) {
                pendingForm.submit();
            }
        });
    }

    function initCharts() {
        if (typeof Chart === "undefined") {
            return;
        }
        document.querySelectorAll("[data-progress-chart]").forEach(function (canvas) {
            var pending = Number(canvas.getAttribute("data-pending"));
            var inProgress = Number(canvas.getAttribute("data-in-progress"));
            var completed = Number(canvas.getAttribute("data-completed"));

            new Chart(canvas, {
                type: "doughnut",
                data: {
                    labels: ["Pending", "In Progress", "Completed"],
                    datasets: [{
                        data: [pending, inProgress, completed],
                        backgroundColor: ["#ffc107", "#0d6efd", "#198754"],
                        borderWidth: 0
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: "bottom"
                        }
                    }
                }
            });
        });
    }

    function autoOpenModal() {
        var modalTarget = document.querySelector("[data-open-modal]");
        if (!modalTarget || typeof bootstrap === "undefined") {
            return;
        }
        var modalElement = document.getElementById(modalTarget.getAttribute("data-open-modal"));
        if (modalElement) {
            new bootstrap.Modal(modalElement).show();
        }
    }

    window.deleteTeam = function(teamId) {
        if (confirm("Are you sure you want to delete this team?")) {
            let form = document.createElement("form");
            form.method = "POST";

            form.action = "/teacher/teams/delete/" + teamId;

            document.body.appendChild(form);
            form.submit();
        }
    };

    /* ================= ADMIN EDIT MODAL ================= */

    document.addEventListener("click", function(e) {

        let btn = e.target.closest("button[data-id]");
        if (!btn) return;

        let id = btn.getAttribute("data-id");
        let name = btn.getAttribute("data-name");
        let email = btn.getAttribute("data-email");
        let contact = btn.getAttribute("data-contact");

        let idField = document.getElementById("editId");
        let nameField = document.getElementById("editName");
        let emailField = document.getElementById("editEmail");
        let contactField = document.getElementById("editContact");

        if (idField) idField.value = id;
        if (nameField) nameField.value = name;
        if (emailField) emailField.value = email;
        if (contactField) contactField.value = contact;

    });

    /* ================= TEAM MEMBER LOGIC ================= */

    let memberIndex = 0;

    function initTeamMembers() {
        let rows = document.querySelectorAll("#memberTableBody tr");
        memberIndex = rows.length;
    }

    // remove member row during create team
    function updateLeaderButtons() {

        document.querySelectorAll("#memberTableBody tr")
            .forEach(row => {

                let radio =
                    row.querySelector("input[type='radio']");

                let removeBtn =
                    row.querySelector(".remove-btn");

                if (!removeBtn || !radio) return;

                if (radio.checked) {

                    removeBtn.disabled = true;
                    removeBtn.classList.add("opacity-50");

                } else {

                    removeBtn.disabled = false;
                    removeBtn.classList.remove("opacity-50");
                }
            });
    }

    document.addEventListener("change", function(e) {

        if (e.target.name === "leaderIndex") {

            updateLeaderButtons();
        }
    });
//  ADD ROW
    function addRow() {

        let table = document.getElementById("memberTableBody");
        if (!table) return;

        let inputs = document.querySelectorAll("input[name$='.enrollment']");
        let last = inputs[inputs.length - 1];

        if (!last || !last.value.trim()) {
            alert("Please enter enrollment first");
            return;
        }

        let values = [];

        for (let i of inputs) {
            let val = i.value.trim().toUpperCase();

            if (val) {
                if (values.includes(val)) {
                    alert("Duplicate enrollment not allowed");
                    return;
                }
                values.push(val);
            }
        }

        let row = table.insertRow();

        row.innerHTML = `
            <td>
                <input type="text"
                       name="members[${memberIndex}].name"
                       class="form-control"
                       required />
            </td>
            
            <td>
                <input type="email"
                       name="members[${memberIndex}].email"
                       class="form-control member-email"
                       required />
                       
                <div class="text-danger small duplicate-email d-none">
                    Email already exists
                </div>
            </td>
            
            <td>
                <input type="tel"
                       name="members[${memberIndex}].contact"
                       class="form-control"
                       maxlength="10"
                       pattern="[0-9]{10}"
                       inputmode="numeric"
                       required />
            </td>
            
            <td>
                <input type="text"
                       name="members[${memberIndex}].enrollment"
                       class="form-control member-enrollment"
                       required />
            
                <div class="text-danger small duplicate-enrollment d-none">
                    Enrollment already exists
                </div>
            </td>
            
            <td class="text-center">
                <input type="radio"
                       name="leaderIndex"
                       value="${memberIndex}"
                       ${document.querySelectorAll("input[name='leaderIndex']:checked").length === 0 ? "checked" : ""}
                       required />
            </td>
            
            <td>
                <button type="button"
                        class="btn btn-danger btn-sm remove-btn">
                    Remove
                </button>
            </td>
        `;

        memberIndex++;
        updateLeaderButtons();
    }
    window.addRow = addRow;

    function showDuplicate(message) {

        let toast = document.getElementById("duplicateToast");
        let btn = document.getElementById("createTeamBtn");

        if (toast) {
            toast.innerText = message;
            toast.classList.remove("d-none");
        }

        if (btn) {
            btn.disabled = true;
            btn.classList.add("disabled");
            btn.style.opacity = "0.5";
            btn.style.pointerEvents = "none";
            btn.style.filter = "blur(1px)";
        }
    }

    function clearDuplicate() {

        let toast = document.getElementById("duplicateToast");
        let btn = document.getElementById("createTeamBtn");

        if (toast) {
            toast.classList.add("d-none");
        }

        if (btn) {
            btn.disabled = false;
            btn.classList.remove("disabled");
            btn.style.opacity = "1";
            btn.style.pointerEvents = "auto";
            btn.style.filter = "none";
        }
    }

    function hasDuplicateValues() {

        let emails = [];
        let enrollments = [];

        let emailInputs =
            document.querySelectorAll("input[name$='.email']");

        let enrollmentInputs =
            document.querySelectorAll("input[name$='.enrollment']");

        for (let input of emailInputs) {

            let val =
                input.value.trim().toLowerCase();

            if (val) {

                if (emails.includes(val)) {

                    showDuplicate(
                        "Duplicate email not allowed"
                    );

                    return true;
                }

                emails.push(val);
            }
        }

        for (let input of enrollmentInputs) {

            let val =
                input.value.trim().toUpperCase();

            if (val) {

                if (enrollments.includes(val)) {

                    showDuplicate(
                        "Duplicate enrollment not allowed"
                    );

                    return true;
                }

                enrollments.push(val);
            }
        }

        clearDuplicate();

        return false;
    }
    document.addEventListener("input", function(e) {

        // contact validation
        if (e.target.name &&
            e.target.name.includes("contact")) {

            e.target.value =
                e.target.value.replace(/[^0-9]/g, '');

            if (e.target.value.length > 10) {

                e.target.value =
                    e.target.value.slice(0, 10);
            }
        }

        hasDuplicateValues();
    });

    document.addEventListener("submit", function(e) {

        let form = e.target;

        if (!form.querySelector("input[name$='.email']")) {
            return;
        }

        if (hasDuplicateValues()) {

            e.preventDefault();

            let btn =
                document.getElementById("createTeamBtn");

            if (btn) {
                btn.disabled = true;
            }

            return;
        }
    });

    document.addEventListener("click", function(e) {

        //  sidebar close (mobile)
        if (window.innerWidth < 992) {
            if (!e.target.closest(".dashboard-sidebar") &&
                !e.target.closest("[data-sidebar-toggle]")) {
                document.body.classList.remove("sidebar-open");
            }
        }

        // 🔹 remove member
        if (e.target.classList.contains("remove-btn")) {

            let row = e.target.closest("tr");
            let radio = row.querySelector("input[type='radio']");

            if (radio && radio.checked) {
                alert("Change leader first, then remove");
                return;
            }

            if (confirm("Remove this member?")) {
                row.remove();
            }
        }

    });

    document.addEventListener("submit", function(e) {

        //  अगर contact input नहीं है → skip (logout, delete etc.)
        if (!e.target.querySelector("input[name*='contact']")) {
            return;
        }

        let inputs = e.target.querySelectorAll("input[name*='contact']");

        for (let input of inputs) {

            if (!/^\d{10}$/.test(input.value)) {
                alert("Invalid contact (must be 10 digits)");
                input.focus();
                e.preventDefault();
                return;
            }
        }

    });

    function setImage(src) {
        document.getElementById("previewImage").src = src;
        document.getElementById("downloadBtn").href = src;
    }
    window.setImage=setImage;

    document.addEventListener("DOMContentLoaded", () => {

        if (typeof flatpickr !== "undefined") {

                flatpickr(".deadline-picker", {
                dateFormat: "Y-m-d",
                altInput: true,
                altFormat: "d/m/Y",
                minDate: "today"
            });


        }

    });

    function validateDuplicates() {

        const emails = document.querySelectorAll(".member-email");
        const enrollments = document.querySelectorAll(".member-enrollment");

        const createBtn =
            document.getElementById("createTeamBtn");

        let hasError = false;

        // RESET
        document.querySelectorAll(".duplicate-email")
            .forEach(e => e.classList.add("d-none"));

        document.querySelectorAll(".duplicate-enrollment")
            .forEach(e => e.classList.add("d-none"));

        // EMAIL CHECK
        let emailSet = [];

        emails.forEach(input => {

            let value = input.value.trim().toLowerCase();

            if (!value) return;

            if (emailSet.includes(value)) {

                hasError = true;

                input.parentElement
                    .querySelector(".duplicate-email")
                    .classList.remove("d-none");
            }

            emailSet.push(value);
        });

        // ENROLLMENT CHECK
        let enrollSet = [];

        enrollments.forEach(input => {

            let value = input.value.trim().toUpperCase();

            if (!value) return;

            if (enrollSet.includes(value)) {

                hasError = true;

                input.parentElement
                    .querySelector(".duplicate-enrollment")
                    .classList.remove("d-none");
            }

            enrollSet.push(value);
        });

        // BUTTON DISABLE
        createBtn.disabled = hasError;

        if (hasError) {

            createBtn.classList.add("opacity-50");

        } else {

            createBtn.classList.remove("opacity-50");
        }
    }
    document.addEventListener("input", function (e) {

        if (
            e.target.classList.contains("member-email") ||
            e.target.classList.contains("member-enrollment")
        ) {
            validateDuplicates();
            clearTimeout(window.duplicateTimer);

            window.duplicateTimer = setTimeout(() => {

                const isEditMode =
                    document.getElementById("editTeamModal");

                if (!isEditMode) {

                    checkDatabaseDuplicates();
                }

            }, 600);
        }
    });
    async function checkDatabaseDuplicates() {

        let emails =
            document.querySelectorAll(".member-email");

        let enrollments =
            document.querySelectorAll(".member-enrollment");

        let hasError = false;

        // EMAIL CHECK
        for (let input of emails) {

            let value =
                input.value.trim();

            let error =
                input.parentElement
                    .querySelector(".duplicate-email");

            const emailRegex =
                /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

            if (!value || !emailRegex.test(value)) {

                error.classList.add("d-none");
                continue;
            }

            let response =
                await fetch(
                    "/teacher/check-email?email="
                    + encodeURIComponent(value)
                );

            let exists =
                (await response.text()) === "EXISTS";

            if (exists) {

                error.classList.remove("d-none");

                hasError = true;

            } else {

                error.classList.add("d-none");
            }
        }

        // ENROLLMENT CHECK
        for (let input of enrollments) {

            let value =
                input.value.trim();

            let error =
                input.parentElement
                    .querySelector(".duplicate-enrollment");

            if (!value || value.length < 12) {

                error.classList.add("d-none");
                continue;
            }

            let response =
                await fetch(
                    "/teacher/check-enrollment?enrollment="
                    + encodeURIComponent(value)
                );

            let exists =
                (await response.text()) === "EXISTS";

            if (exists) {

                error.classList.remove("d-none");

                hasError = true;

            } else {

                error.classList.add("d-none");
            }
        }

        let btn =
            document.getElementById("createTeamBtn");

        if (btn) {

            btn.disabled = hasError;

            btn.style.opacity =
                hasError ? "0.5" : "1";

            btn.style.pointerEvents =
                hasError ? "none" : "auto";
        }
    }

    async function checkTeacherEmail(input) {

        const value =
            input.value.trim();

        const error =
            document.querySelector(
                ".duplicate-teacher-email"
            );

        const btn =
            document.getElementById(
                "createTeacherBtn"
            );

        const emailRegex =
            /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        if (!value || !emailRegex.test(value)) {

            error.classList.add("d-none");

            btn.disabled = false;

            btn.style.opacity = "1";

            return;
        }

        const response =
            await fetch(
                "/admin/check-teacher-email?email="
                + encodeURIComponent(value)
            );

        const exists =
            (await response.text()) === "EXISTS";

        if (exists) {

            error.classList.remove("d-none");

            btn.disabled = true;

            btn.style.opacity = "0.5";

        } else {

            error.classList.add("d-none");

            btn.disabled = false;

            btn.style.opacity = "1";
        }
    }

    document.addEventListener("input", (e) => {

        if (
            e.target.classList.contains(
                "teacher-email"
            )
        ) {

            clearTimeout(
                window.teacherEmailTimer
            );

            window.teacherEmailTimer =
                setTimeout(() => {

                    checkTeacherEmail(
                        e.target
                    );

                }, 600);
        }
    });

    ready(function () {
        initSidebar();
        enhanceTables();
        initToasts();
        initConfirmation();
        initCharts();
        autoOpenModal();
        initTeamMembers();
        updateLeaderButtons();
    });
})();
