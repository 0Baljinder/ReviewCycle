import math

x = 0.0

print("x\t\tSin(x)\t\tCos(x)\t\tTan(x)")
print("---------------------------------------------------------------------------------------------------------------------")

while x <= 10:
    radians = math.radians(x)
    
    sine = math.sin(radians)
    
    cousine = math.cos(radians)
    
    tangent = math.tan(radians)
    
    print(f"{x:.1f}\t\t{sine:.4f}\t\t{cousine:.4f}\t\t{tangent:.4f}")
    x += 0.2