/*
 * Copyright (c) 1998-2012 Caucho Technology -- all rights reserved
 *
 * This file is part of Resin(R) Open Source
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Resin Open Source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Resin Open Source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
 * of NON-INFRINGEMENT.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Resin Open Source; if not, write to the
 *
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Scott Ferguson
 */

package com.caucho.quercus.expr;

import com.caucho.quercus.gen.PhpWriter;

import java.io.IOException;

/**
 * Represents a PHP equality testing expression.
 */
public class BinaryEqualsExprPro extends BinaryEqualsExpr
  implements ExprPro
{
  public BinaryEqualsExprPro(Expr left, Expr right)
  {
    super(left, right);
  }

  public ExprGenerator getGenerator()
  {
    return GENERATOR;
  }

  private ExprGenerator GENERATOR = new AbstractBinaryGenerateExpr(getLocation()) {
      public ExprGenerator getLeft()
      {
	return ((ExprPro) _left).getGenerator();
      }
      
      public ExprGenerator getRight()
      {
	return ((ExprPro) _right).getGenerator();
      }

      /**
       * Generates code to evaluate the expression
       *
       * @param out the writer to the Java source code.
       */
      public void generate(PhpWriter out)
	throws IOException
      {
	out.print("env.toValue(");
	generateBoolean(out);
	out.print(")");
      }

      /**
       * Generates code to evaluate the expression.
       *
       * @param out the writer to the Java source code.
       */
      public void generateBoolean(PhpWriter out)
	throws IOException
      {
	getLeft().generate(out);
	out.print(".eql(");
	getRight().generateValue(out);
	out.print(")");
      }
    };
}

