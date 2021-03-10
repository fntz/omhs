# methods generator

# (1..22).each do |index|
#   t = (1..index).map { |x| %Q{T#{x}: c.WeakTypeTag,} }.join(" \n")
#   ts = (1..index).map { |x| %Q{T#{x}} }.join(", ")
#   r = %Q{
#     def run#{index}[#{t} R: c.WeakTypeTag](c: whitebox.Context)
#                                               (f: c.Expr[(#{ts}) => R]): c.Expr[ExecutableRule] = {
#       generate(c)(f.tree)
#     }
#   }
#   puts r 
# end  

(1..22).each do |index|
  t = (1..index).map { |x| "T#{x}" }.join(", ")
  r = %Q{implicit def ~>[#{t}, R](f: (#{t}) => R): ExecutableRule =
      macro RoutingImpl.run#{index}[#{t}, R]}
  puts r
end  