# ========================================
# SCRIPT DE COPIE AUTOMATIQUE DU FRAMEWORK
# ========================================

# Configuration
$SOURCE = "C:\Users\tandr\Downloads\Framework\framework\src\main\java\com\nam\java"
$DEST_BASE = "C:\Users\tandr\OneDrive\Documents\Framework-mvc\Framework\src\main\java\mg\itu\framework"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "COPIE DU FRAMEWORK" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Créer la structure de dossiers
Write-Host "Création de la structure..." -ForegroundColor Yellow
$folders = @(
    "$DEST_BASE",
    "$DEST_BASE\annotation",
    "$DEST_BASE\servlet", 
    "$DEST_BASE\model",
    "$DEST_BASE\util"
)

foreach ($folder in $folders) {
    if (!(Test-Path $folder)) {
        New-Item -Path $folder -ItemType Directory -Force | Out-Null
        Write-Host "✓ Créé : $folder" -ForegroundColor Green
    }
}

# Mapping des fichiers vers leurs destinations
$fileMapping = @{
    "UrlListener.java" = "$DEST_BASE\servlet"
    "AnotationReader.java" = "$DEST_BASE\util"
    "PackageReader.java" = "$DEST_BASE\util"
    "Transtipation.java" = "$DEST_BASE\util"
    "ModelView.java" = "$DEST_BASE\model"
    "Url.java" = "$DEST_BASE\model"
    "MyAnnotation.java" = "$DEST_BASE\annotation"
    "MyParam.java" = "$DEST_BASE\annotation"
    "HttpMethod.java" = "$DEST_BASE\annotation"
}

Write-Host "`nCopie et modification des fichiers..." -ForegroundColor Yellow

# Copier et modifier chaque fichier
foreach ($file in $fileMapping.Keys) {
    $sourcePath = Join-Path $SOURCE $file
    $destFolder = $fileMapping[$file]
    $destPath = Join-Path $destFolder $file
    
    if (Test-Path $sourcePath) {
        # Lire le contenu
        $content = Get-Content $sourcePath -Raw -Encoding UTF8
        
        # Remplacements nécessaires
        $content = $content -replace 'package com\.nam\.java;', 'package mg.itu.framework;'
        
        # Ajuster les imports selon le fichier
        if ($file -eq "UrlListener.java") {
            $content = $content -replace 'package mg\.itu\.framework;', 'package mg.itu.framework.servlet;'
            $content = $content -replace 'import com\.nam\.java\.', 'import mg.itu.framework.'
        }
        elseif ($file -in @("AnotationReader.java", "PackageReader.java", "Transtipation.java")) {
            $content = $content -replace 'package mg\.itu\.framework;', 'package mg.itu.framework.util;'
            $content = $content -replace 'import com\.nam\.java\.', 'import mg.itu.framework.'
        }
        elseif ($file -in @("ModelView.java", "Url.java")) {
            $content = $content -replace 'package mg\.itu\.framework;', 'package mg.itu.framework.model;'
        }
        elseif ($file -in @("MyAnnotation.java", "MyParam.java", "HttpMethod.java")) {
            $content = $content -replace 'package mg\.itu\.framework;', 'package mg.itu.framework.annotation;'
        }
        
        # Corriger tous les imports restants
        $content = $content -replace 'import com\.nam\.java\.AnotationReader;', 'import mg.itu.framework.util.AnotationReader;'
        $content = $content -replace 'import com\.nam\.java\.PackageReader;', 'import mg.itu.framework.util.PackageReader;'
        $content = $content -replace 'import com\.nam\.java\.Transtipation;', 'import mg.itu.framework.util.Transtipation;'
        $content = $content -replace 'import com\.nam\.java\.ModelView;', 'import mg.itu.framework.model.ModelView;'
        $content = $content -replace 'import com\.nam\.java\.Url;', 'import mg.itu.framework.model.Url;'
        $content = $content -replace 'import com\.nam\.java\.MyAnnotation;', 'import mg.itu.framework.annotation.MyAnnotation;'
        $content = $content -replace 'import com\.nam\.java\.MyParam;', 'import mg.itu.framework.annotation.MyParam;'
        $content = $content -replace 'import com\.nam\.java\.HttpMethod;', 'import mg.itu.framework.annotation.HttpMethod;'
        
        # Sauvegarder avec encodage UTF-8
        $content | Out-File -FilePath $destPath -Encoding UTF8 -NoNewline
        
        Write-Host "✓ Copié : $file → $($destFolder.Split('\')[-1])\" -ForegroundColor Green
    } else {
        Write-Host "✗ Non trouvé : $file" -ForegroundColor Red
    }
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "COPIE TERMINÉE !" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "Prochaine étape : Compiler le framework" -ForegroundColor Yellow
Write-Host "Commande : cd C:\Users\tandr\OneDrive\Documents\Framework-mvc\Framework" -ForegroundColor Gray
Write-Host "           .\compile-framework.ps1`n" -ForegroundColor Gray