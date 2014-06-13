-------------------------------------------------------------------------------
-- Copyright (c) 2013 Synflow SAS.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--    Matthieu Wipliez - initial API and implementation and/or initial documentation
-------------------------------------------------------------------------------

-------------------------------------------------------------------------------
-- Title      : Adds VHDL-2008 like constructs in a VHDL'93 compatible way
-- Author     : Matthieu Wipliez (matthieu.wipliez@synflow.com)
-- Standard   : VHDL'93
-------------------------------------------------------------------------------

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
use std.textio.all;
-------------------------------------------------------------------------------


-------------------------------------------------------------------------------
-- 
-------------------------------------------------------------------------------
package Helper_functions is

  function to_boolean(b : std_logic) return boolean;
  function to_std_logic(b : boolean) return std_logic;
  function to_string_93(b : bit) return string;
  function to_hstring_93(b : bit_vector) return string;

end Helper_functions;

-------------------------------------------------------------------------------
-- Body of package
-------------------------------------------------------------------------------
package body Helper_functions is

  -----------------------------------------------------------------------------
  -- Built-in constants and functions
  -----------------------------------------------------------------------------

  function to_boolean(b : std_logic) return boolean is
  begin
    return b = '1';
  end;

  function to_std_logic(b : boolean) return std_logic is
  begin
    if b then
      return '1';
    else
      return '0';
    end if;
  end;

  function to_string_93(b : bit) return string is begin
    -- rtl_synthesis off
    -- synthesis translate_off
    return to_string(b);
    -- synthesis translate_on
    -- rtl_synthesis on
    return "";
  end;

  function to_hstring_93(b : bit_vector) return string is begin
    -- rtl_synthesis off
    -- synthesis translate_off
    return to_hstring(b);
    -- synthesis translate_on
    -- rtl_synthesis on
    return "";
  end;

end Helper_functions;
