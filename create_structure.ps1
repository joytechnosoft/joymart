$BASE = "app/src/main/java/com/jminnovatech/joymart"

$folders = @(
    "core/ui/theme",
    "core/ui/components",
    "core/util",
    "core/session",
    "data/remote",
    "data/local/entity",
    "data/model",
    "data/repository",
    "domain/model",
    "domain/usecase",
    "ui/navigation",
    "ui/splash",
    "ui/auth",
    "ui/customer",
    "ui/components",
    "worker"
)

foreach ($folder in $folders) {
    $path = Join-Path $BASE $folder
    if (!(Test-Path $path)) {
        New-Item -ItemType Directory -Path $path -Force | Out-Null
    }
}

Write-Host "✅ JOyMartApp structure created successfully"
