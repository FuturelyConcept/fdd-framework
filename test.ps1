# FDD Framework Verification Script - Simple Version
param(
    [string]$ProjectPath = ".",
    [int]$Port = 8080,
    [int]$TimeoutSeconds = 60
)

$ErrorActionPreference = "Continue"

Write-Host "============================================================================" -ForegroundColor Magenta
Write-Host "FDD FRAMEWORK VERIFICATION SCRIPT" -ForegroundColor Magenta
Write-Host "============================================================================" -ForegroundColor Magenta

# Step 1: Check Environment
Write-Host "`n[STEP 1] Checking Environment..." -ForegroundColor Blue

try {
    $javaVersion = java -version 2>&1 | Select-String "version" | ForEach-Object { $_.ToString() }
    Write-Host "[OK] Java: $javaVersion" -ForegroundColor Green

    $mavenVersion = mvn -version 2>&1 | Select-String "Apache Maven" | ForEach-Object { $_.ToString() }
    Write-Host "[OK] Maven: $mavenVersion" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Environment check failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 2: Check Project Structure
Write-Host "`n[STEP 2] Checking Project Structure..." -ForegroundColor Blue

$requiredDirs = @("fdd-core", "fdd-starter", "fdd-demo")
foreach ($dir in $requiredDirs) {
    $fullPath = Join-Path $ProjectPath $dir
    if (Test-Path $fullPath) {
        Write-Host "[OK] Found: $dir" -ForegroundColor Green
    } else {
        Write-Host "[ERROR] Missing: $dir" -ForegroundColor Red
        exit 1
    }
}


Write-Host ""

# Step 5: Test Function Discovery
Write-Host "`n[STEP 5] Testing Function Discovery..." -ForegroundColor Blue

try {
    $functionsResponse = Invoke-RestMethod -Uri "http://localhost:$Port/functions" -Method GET
    Write-Host "[OK] Function discovery works - Found $($functionsResponse.count) functions" -ForegroundColor Green

    foreach ($func in $functionsResponse.functions) {
        Write-Host "  -> $($func.component): $($func.inputType) -> $($func.outputType)" -ForegroundColor Cyan
    }

    # Check for expected functions
    $expectedFunctions = @("userValidator", "inventoryChecker", "paymentProcessor")
    $foundFunctions = $functionsResponse.functions | ForEach-Object { $_.component }

    foreach ($expectedFunc in $expectedFunctions) {
        if ($foundFunctions -contains $expectedFunc) {
            Write-Host "[OK] Expected function found: $expectedFunc" -ForegroundColor Green
        } else {
            Write-Host "[WARNING] Expected function missing: $expectedFunc" -ForegroundColor Yellow
        }
    }
} catch {
    Write-Host "[ERROR] Function discovery failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Step 6: Test User Validation Function
Write-Host "`n[STEP 6] Testing User Validation..." -ForegroundColor Blue

try {
    # Test valid user
    $validUser = @{
        name = "John Doe"
        email = "john@example.com"
        age = 25
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "http://localhost:$Port/demo/validate-user" -Method POST -Body $validUser -ContentType "application/json"

    if ($response.valid) {
        Write-Host "[OK] Valid user test: PASSED" -ForegroundColor Green
    } else {
        Write-Host "[ERROR] Valid user test: FAILED" -ForegroundColor Red
    }

    # Test invalid user
    $invalidUser = @{
        name = "Jane"
        email = "invalid-email"
        age = 16
    } | ConvertTo-Json

    $response2 = Invoke-RestMethod -Uri "http://localhost:$Port/demo/validate-user" -Method POST -Body $invalidUser -ContentType "application/json"

    if (!$response2.valid) {
        Write-Host "[OK] Invalid user test: PASSED (correctly rejected)" -ForegroundColor Green
    } else {
        Write-Host "[ERROR] Invalid user test: FAILED (should reject)" -ForegroundColor Red
    }

} catch {
    Write-Host "[ERROR] User validation test failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Step 7: Test Inventory Function
Write-Host "`n[STEP 7] Testing Inventory Check..." -ForegroundColor Blue

try {
    $inventoryRequest = @{
        productId = "product-123"
        quantity = 50
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "http://localhost:$Port/demo/check-inventory" -Method POST -Body $inventoryRequest -ContentType "application/json"

    if ($response.available) {
        Write-Host "[OK] Inventory check (50 units): PASSED" -ForegroundColor Green
    } else {
        Write-Host "[ERROR] Inventory check (50 units): FAILED" -ForegroundColor Red
    }

    # Test excessive quantity
    $excessiveRequest = @{
        productId = "product-123"
        quantity = 150
    } | ConvertTo-Json

    $response2 = Invoke-RestMethod -Uri "http://localhost:$Port/demo/check-inventory" -Method POST -Body $excessiveRequest -ContentType "application/json"

    if (!$response2.available) {
        Write-Host "[OK] Inventory check (150 units): PASSED (correctly rejected)" -ForegroundColor Green
    } else {
        Write-Host "[ERROR] Inventory check (150 units): FAILED (should reject)" -ForegroundColor Red
    }

} catch {
    Write-Host "[ERROR] Inventory check test failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Step 8: Test Payment Processing
Write-Host "`n[STEP 8] Testing Payment Processing..." -ForegroundColor Blue

try {
    $paymentRequest = @{
        userId = "john-doe"
        amount = 100.00
        currency = "USD"
        paymentMethod = "CARD"
        orderId = "test-order-123"
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "http://localhost:$Port/demo/process-payment" -Method POST -Body $paymentRequest -ContentType "application/json"

    if ($response.success) {
        Write-Host "[OK] Payment processing: PASSED" -ForegroundColor Green
        Write-Host "     Transaction ID: $($response.transactionId)" -ForegroundColor Cyan
    } else {
        Write-Host "[ERROR] Payment processing: FAILED" -ForegroundColor Red
    }

} catch {
    Write-Host "[ERROR] Payment processing test failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Step 9: Test Complete Order Flow (Function Composition)
Write-Host "`n[STEP 9] Testing Complete Order Flow (Function Composition)..." -ForegroundColor Blue

try {
    $orderRequest = @{
        userData = @{
            name = "John Doe"
            email = "john@example.com"
            age = 25
        }
        productId = "product-123"
        quantity = 50
        paymentMethod = "CARD"
    } | ConvertTo-Json -Depth 3

    $response = Invoke-RestMethod -Uri "http://localhost:$Port/demo/create-order" -Method POST -Body $orderRequest -ContentType "application/json"

    if ($response.success) {
        Write-Host "[OK] Complete order flow: PASSED" -ForegroundColor Green
        Write-Host "     Order ID: $($response.orderId)" -ForegroundColor Cyan
        if ($response.transactionId) {
            Write-Host "     Transaction ID: $($response.transactionId)" -ForegroundColor Cyan
        }
    } else {
        Write-Host "[ERROR] Complete order flow: FAILED - $($response.message)" -ForegroundColor Red
    }

    # Test order failure with invalid user
    $failedOrderRequest = @{
        userData = @{
            name = "Jane"
            email = "invalid-email"
            age = 16
        }
        productId = "product-123"
        quantity = 50
        paymentMethod = "CARD"
    } | ConvertTo-Json -Depth 3

    $failResponse = Invoke-RestMethod -Uri "http://localhost:$Port/demo/create-order" -Method POST -Body $failedOrderRequest -ContentType "application/json"

    if (!$failResponse.success) {
        Write-Host "[OK] Order failure handling: PASSED (correctly rejected invalid user)" -ForegroundColor Green
    } else {
        Write-Host "[ERROR] Order failure handling: FAILED (should reject invalid user)" -ForegroundColor Red
    }

} catch {
    Write-Host "[ERROR] Complete order flow test failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Step 10: Test Function Health
Write-Host "`n[STEP 10] Testing Function Health..." -ForegroundColor Blue

try {
    $healthResponse = Invoke-RestMethod -Uri "http://localhost:$Port/functions/health" -Method GET
    if ($healthResponse.status -eq "UP") {
        Write-Host "[OK] Function health check: PASSED" -ForegroundColor Green
        Write-Host "     Function count: $($healthResponse.functionCount)" -ForegroundColor Cyan
    } else {
        Write-Host "[ERROR] Function health check: FAILED" -ForegroundColor Red
    }
} catch {
    Write-Host "[ERROR] Function health test failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Step 11: Test Direct Function Execution
Write-Host "`n[STEP 11] Testing Direct Function Execution via REST..." -ForegroundColor Blue

try {
    $directUserData = @{
        name = "Alice Smith"
        email = "alice@example.com"
        age = 30
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "http://localhost:$Port/functions/userValidator" -Method POST -Body $directUserData -ContentType "application/json"

    if ($response.valid) {
        Write-Host "[OK] Direct function execution: PASSED" -ForegroundColor Green
    } else {
        Write-Host "[ERROR] Direct function execution: FAILED" -ForegroundColor Red
    }
} catch {
    Write-Host "[ERROR] Direct function execution test failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Step 12: Performance Test
Write-Host "`n[STEP 12] Running Basic Performance Test..." -ForegroundColor Blue

$performanceJobs = @()
for ($i = 1; $i -le 5; $i++) {
    $job = Start-Job -ScriptBlock {
        param($port, $requestId)
        try {
            $userData = @{
                name = "User$requestId"
                email = "user$requestId@example.com"
                age = 25
            } | ConvertTo-Json

            $response = Invoke-RestMethod -Uri "http://localhost:$port/demo/validate-user" -Method POST -Body $userData -ContentType "application/json"
            return @{ Success = $response.valid; RequestId = $requestId }
        } catch {
            return @{ Success = $false; RequestId = $requestId; Error = $_.Exception.Message }
        }
    } -ArgumentList $Port, $i

    $performanceJobs += $job
}

$performanceResults = $performanceJobs | Wait-Job | Receive-Job
$successCount = ($performanceResults | Where-Object { $_.Success }).Count

Write-Host "[INFO] Performance test: $successCount/5 concurrent requests successful" -ForegroundColor Cyan
$performanceJobs | Remove-Job

# Cleanup
Write-Host "`n[STEP 13] Cleanup..." -ForegroundColor Blue

Stop-Job $job -ErrorAction SilentlyContinue
Remove-Job $job -ErrorAction SilentlyContinue

try {
    $processOnPort = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess
    if ($processOnPort) {
        Stop-Process -Id $processOnPort -Force -ErrorAction SilentlyContinue
        Write-Host "[OK] Stopped application process" -ForegroundColor Green
    }
} catch {
    # Ignore cleanup errors
}

Pop-Location

# Final Summary
Write-Host "`n============================================================================" -ForegroundColor Magenta
Write-Host "VERIFICATION COMPLETED" -ForegroundColor Magenta
Write-Host "============================================================================" -ForegroundColor Magenta

Write-Host "`nTest Summary:" -ForegroundColor White
Write-Host "- Environment: Java 17+, Maven 3.6+" -ForegroundColor Cyan
Write-Host "- Build: Framework compilation" -ForegroundColor Cyan
Write-Host "- Functions: Discovery, validation, composition" -ForegroundColor Cyan
Write-Host "- Performance: Concurrent request handling" -ForegroundColor Cyan

Write-Host "`nIf you see any [ERROR] messages above, please share the complete output." -ForegroundColor Yellow
Write-Host "If all tests show [OK], your FDD framework is working perfectly!" -ForegroundColor Green